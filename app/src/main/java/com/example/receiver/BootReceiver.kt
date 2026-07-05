package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.service.SpeakingClockService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device boot completed. Checking if service should be restarted.")
            val sharedPrefs = context.getSharedPreferences("speaking_clock_prefs", Context.MODE_PRIVATE)
            val serviceEnabled = sharedPrefs.getBoolean("service_enabled", false)
            
            if (serviceEnabled) {
                val serviceIntent = Intent(context, SpeakingClockService::class.java).apply {
                    action = SpeakingClockService.ACTION_START_SERVICE
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d("BootReceiver", "Speaking clock service restarted successfully on boot.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to auto-start service on boot: ${e.message}")
                }
            }
        }
    }
}
