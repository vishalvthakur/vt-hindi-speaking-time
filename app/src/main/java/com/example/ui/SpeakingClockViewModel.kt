package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AnnouncementLog
import com.example.data.AppDatabase
import com.example.data.AnnouncementRepository
import com.example.service.SpeakingClockService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SpeakingClockViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = AnnouncementRepository(database.announcementDao())
    private val sharedPrefs: SharedPreferences = application.getSharedPreferences("speaking_clock_prefs", Context.MODE_PRIVATE)

    private val _isServiceActive = MutableStateFlow(false)
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    private val _screenWakeEnabled = MutableStateFlow(true)
    val screenWakeEnabled: StateFlow<Boolean> = _screenWakeEnabled.asStateFlow()

    private val _intervalMinutes = MutableStateFlow(0)
    val intervalMinutes: StateFlow<Int> = _intervalMinutes.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(1.0f)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _selectedVoiceName = MutableStateFlow("")
    val selectedVoiceName: StateFlow<String> = _selectedVoiceName.asStateFlow()

    private val _uiLanguage = MutableStateFlow("en")
    val uiLanguage: StateFlow<String> = _uiLanguage.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<String>>(emptyList())
    val availableVoices: StateFlow<List<String>> = _availableVoices.asStateFlow()

    val historyLogs: StateFlow<List<AnnouncementLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var tempTts: TextToSpeech? = null

    init {
        loadSettings()
        queryHindiVoices()
    }

    private fun loadSettings() {
        _isServiceActive.value = sharedPrefs.getBoolean("service_enabled", false)
        _screenWakeEnabled.value = sharedPrefs.getBoolean("screen_wake_enabled", true)
        _intervalMinutes.value = sharedPrefs.getInt("interval_minutes", 0)
        _speechRate.value = sharedPrefs.getFloat("speech_rate", 1.0f)
        _speechPitch.value = sharedPrefs.getFloat("speech_pitch", 1.0f)
        _selectedVoiceName.value = sharedPrefs.getString("selected_voice_name", "") ?: ""
        _uiLanguage.value = sharedPrefs.getString("ui_language", "en") ?: "en"
    }

    private fun queryHindiVoices() {
        tempTts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val hindiVoices = tempTts?.voices?.filter { voice ->
                    voice.locale.language == "hi"
                }?.map { it.name } ?: emptyList()
                _availableVoices.value = hindiVoices
                tempTts?.shutdown()
                tempTts = null
            }
        }
    }

    fun toggleService(context: Context) {
        val newState = !isServiceActive.value
        sharedPrefs.edit().putBoolean("service_enabled", newState).apply()
        _isServiceActive.value = newState

        val intent = Intent(context, SpeakingClockService::class.java)
        if (newState) {
            intent.action = SpeakingClockService.ACTION_START_SERVICE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            intent.action = SpeakingClockService.ACTION_STOP_SERVICE
            context.stopService(intent)
        }
    }

    fun updateScreenWakeEnabled(context: Context, enabled: Boolean) {
        sharedPrefs.edit().putBoolean("screen_wake_enabled", enabled).apply()
        _screenWakeEnabled.value = enabled
        notifyServiceUpdate(context)
    }

    fun updateInterval(context: Context, minutes: Int) {
        sharedPrefs.edit().putInt("interval_minutes", minutes).apply()
        _intervalMinutes.value = minutes
        notifyServiceUpdate(context)
    }

    fun updateSpeechRate(context: Context, rate: Float) {
        sharedPrefs.edit().putFloat("speech_rate", rate).apply()
        _speechRate.value = rate
        notifyServiceUpdate(context)
    }

    fun updateSpeechPitch(context: Context, pitch: Float) {
        sharedPrefs.edit().putFloat("speech_pitch", pitch).apply()
        _speechPitch.value = pitch
        notifyServiceUpdate(context)
    }

    fun updateSelectedVoice(context: Context, voiceName: String) {
        sharedPrefs.edit().putString("selected_voice_name", voiceName).apply()
        _selectedVoiceName.value = voiceName
        notifyServiceUpdate(context)
    }

    fun updateUiLanguage(language: String) {
        sharedPrefs.edit().putString("ui_language", language).apply()
        _uiLanguage.value = language
    }

    private fun notifyServiceUpdate(context: Context) {
        if (isServiceActive.value) {
            val intent = Intent(context, SpeakingClockService::class.java).apply {
                action = SpeakingClockService.ACTION_UPDATE_SETTINGS
            }
            context.startService(intent)
        }
    }

    fun testSpeech(context: Context) {
        val intent = Intent(context, SpeakingClockService::class.java).apply {
            action = SpeakingClockService.ACTION_SPEAK_TEST
        }
        if (isServiceActive.value) {
            context.startService(intent)
        } else {
            // Automatically start the service, speak, and leave it running
            sharedPrefs.edit().putBoolean("service_enabled", true).apply()
            _isServiceActive.value = true
            
            val startIntent = Intent(context, SpeakingClockService::class.java).apply {
                action = SpeakingClockService.ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent)
            } else {
                context.startService(startIntent)
            }
            
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                context.startService(intent)
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun deleteLog(id: Int) {
        viewModelScope.launch {
            repository.deleteLogById(id)
        }
    }
}
