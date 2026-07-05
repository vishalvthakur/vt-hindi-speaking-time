package com.example.util

import java.util.Calendar

object TimeFormatter {
    fun getCurrentTimeInHindi(): String {
        val calendar = Calendar.getInstance()
        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val period = when (hour24) {
            in 5..11 -> "सुबह"     // Morning (5 AM to 11:59 AM)
            in 12..16 -> "दोपहर"   // Afternoon (12 PM to 4:59 PM)
            in 17..19 -> "शाम"     // Evening (5 PM to 7:59 PM)
            else -> "रात"         // Night (8 PM to 4:59 AM)
        }

        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        return if (minute == 0) {
            "अभी $period के पूरे $hour12 बज रहे हैं।"
        } else {
            "अभी $period के $hour12 बजकर $minute मिनट हो रहे हैं।"
        }
    }
}
