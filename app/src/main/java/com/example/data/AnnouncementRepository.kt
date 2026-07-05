package com.example.data

import kotlinx.coroutines.flow.Flow

class AnnouncementRepository(private val dao: AnnouncementDao) {
    val allLogs: Flow<List<AnnouncementLog>> = dao.getAllLogs()

    suspend fun insertLog(log: AnnouncementLog) {
        dao.insertLog(log)
    }

    suspend fun clearLogs() {
        dao.clearAllLogs()
    }

    suspend fun deleteLogById(id: Int) {
        dao.deleteLogById(id)
    }
}
