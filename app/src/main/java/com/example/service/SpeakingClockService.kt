package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.AnnouncementLog
import com.example.util.TimeFormatter
import kotlinx.coroutines.*
import java.util.Calendar
import java.util.Locale

class SpeakingClockService : Service(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var sharedPrefs: SharedPreferences
    
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var periodicJob: Job? = null
    private var isTtsReady = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_ON) {
                val screenWakeEnabled = sharedPrefs.getBoolean("screen_wake_enabled", true)
                if (screenWakeEnabled) {
                    speakTime("SCREEN_WAKE")
                }
            }
        }
    }

    companion object {
        const val ACTION_START_SERVICE = "com.example.action.START_SERVICE"
        const val ACTION_UPDATE_SETTINGS = "com.example.action.UPDATE_SETTINGS"
        const val ACTION_STOP_SERVICE = "com.example.action.STOP_SERVICE"
        const val ACTION_SPEAK_TEST = "com.example.action.SPEAK_TEST"
        
        const val NOTIFICATION_CHANNEL_ID = "speaking_clock_service_channel"
        const val NOTIFICATION_ID = 4466
        private const val TAG = "SpeakingClockService"
    }

    override fun onCreate() {
        super.onCreate()
        sharedPrefs = getSharedPreferences("speaking_clock_prefs", Context.MODE_PRIVATE)
        
        // Initialize TextToSpeech
        tts = TextToSpeech(this, this)

        // Register screen wake receiver
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                startForegroundServiceWithNotification()
                startPeriodicCheck()
            }
            ACTION_UPDATE_SETTINGS -> {
                applySelectedVoice()
                startPeriodicCheck()
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
            ACTION_SPEAK_TEST -> {
                speakTime("TEST")
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Hindi language is not supported or missing data")
            } else {
                isTtsReady = true
                applySelectedVoice()
            }
        } else {
            Log.e(TAG, "TTS Initialization failed")
        }
    }

    private fun applySelectedVoice() {
        if (!isTtsReady) return
        
        val speechRate = sharedPrefs.getFloat("speech_rate", 1.0f)
        val speechPitch = sharedPrefs.getFloat("speech_pitch", 1.0f)
        tts.setSpeechRate(speechRate)
        tts.setPitch(speechPitch)

        val savedVoiceName = sharedPrefs.getString("selected_voice_name", "") ?: ""
        val voices = tts.voices
        if (!voices.isNullOrEmpty()) {
            var selectedVoice: android.speech.tts.Voice? = null
            if (savedVoiceName.isNotEmpty()) {
                selectedVoice = voices.find { it.name == savedVoiceName }
            }
            
            if (selectedVoice == null) {
                // Autodetect female Hindi voice
                selectedVoice = voices.find { voice ->
                    voice.locale.language == "hi" && 
                    (voice.name.contains("female", ignoreCase = true) || 
                     voice.name.contains("-f-", ignoreCase = true) ||
                     voice.name.contains("hie", ignoreCase = true) || 
                     voice.name.contains("hic", ignoreCase = true))
                }
            }

            if (selectedVoice == null) {
                // Fallback to any Hindi voice
                selectedVoice = voices.find { it.locale.language == "hi" }
            }

            if (selectedVoice != null) {
                try {
                    tts.voice = selectedVoice
                    Log.d(TAG, "Voice selected: ${selectedVoice.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to set selected voice: ${e.message}")
                }
            }
        }
    }

    private fun startPeriodicCheck() {
        periodicJob?.cancel()
        val interval = sharedPrefs.getInt("interval_minutes", 0)
        if (interval <= 0) return

        periodicJob = serviceScope.launch {
            var lastSpokenMinute = -1
            while (isActive) {
                val calendar = Calendar.getInstance()
                val currentMinute = calendar.get(Calendar.MINUTE)
                
                if (currentMinute % interval == 0 && currentMinute != lastSpokenMinute) {
                    lastSpokenMinute = currentMinute
                    speakTime("INTERVAL")
                }
                delay(30000L) // check every 30 seconds
            }
        }
    }

    private fun speakTime(triggerType: String) {
        if (!isTtsReady) {
            Log.e(TAG, "Cannot speak. TTS is not ready.")
            saveLogToDatabase(TimeFormatter.getCurrentTimeInHindi(), triggerType, "FAILED (TTS Not Ready)")
            return
        }

        val text = TimeFormatter.getCurrentTimeInHindi()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SpeakingClockId_${System.currentTimeMillis()}")
        }

        val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID))
        val status = if (result == TextToSpeech.SUCCESS) "SUCCESS" else "FAILED"
        saveLogToDatabase(text, triggerType, status)
    }

    private fun saveLogToDatabase(text: String, triggerType: String, status: String) {
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                db.announcementDao().insertLog(
                    AnnouncementLog(
                        textSpoken = text,
                        triggerType = triggerType,
                        status = status
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save announcement log: ${e.message}")
            }
        }
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = NOTIFICATION_CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Hindi Speaking Clock"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Keeps the speaking clock active in the background."
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Hindi Speaking Clock is Active")
            .setContentText("Speaking time on screen wake and custom intervals.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        periodicJob?.cancel()
        serviceJob.cancel()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Receiver not registered or already unregistered")
        }
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
