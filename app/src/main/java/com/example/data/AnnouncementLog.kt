package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcement_logs")
data class AnnouncementLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val textSpoken: String,
    val triggerType: String, // SCREEN_WAKE, INTERVAL, TEST
    val status: String // SUCCESS, FAILED
)
