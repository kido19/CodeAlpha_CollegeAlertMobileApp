package com.example.collagealert

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlertRepository(private val alertDao: AlertDao) {

    val allAlerts: LiveData<List<AlertEntity>> = alertDao.getAllAlerts()
    val unreadCount: LiveData<Int> = alertDao.getUnreadCount()
    val totalCount: LiveData<Int> = alertDao.getTotalCount()

    suspend fun getAlertById(id: String): AlertEntity? {
        return withContext(Dispatchers.IO) {
            alertDao.getAlertById(id)
        }
    }

    suspend fun getAlertByTimestampAndTitle(timestamp: Long, title: String): AlertEntity? {
        return withContext(Dispatchers.IO) {
            alertDao.getAlertByTimestampAndTitle(timestamp, title)
        }
    }

    suspend fun insert(alert: AlertData) {
        withContext(Dispatchers.IO) {
            // Check if it was previously deleted
            val existing = alertDao.getAlertById(alert.id)
            if (existing != null && existing.isDeleted) {
                return@withContext
            }
            alertDao.insert(AlertEntity.fromAlertData(alert))
        }
    }

    suspend fun update(alert: AlertData) {
        withContext(Dispatchers.IO) {
            alertDao.update(AlertEntity.fromAlertData(alert))
        }
    }

    suspend fun delete(alert: AlertData) {
        withContext(Dispatchers.IO) {
            // Use soft delete so it doesn't reappear on sync
            alertDao.softDelete(alert.id)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            alertDao.deleteAll()
        }
    }
}