package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcement_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AnnouncementLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AnnouncementLog)

    @Query("DELETE FROM announcement_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM announcement_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)
}
