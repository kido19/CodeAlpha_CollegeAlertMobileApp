package com.example.collagealert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.UUID

class MainViewModel(private val repository: AlertRepository) : ViewModel() {

    val allAlerts: LiveData<List<AlertEntity>> = repository.allAlerts
    val unreadCount: LiveData<Int> = repository.unreadCount
    val totalCount: LiveData<Int> = repository.totalCount

    private val _alerts = MutableLiveData<List<AlertData>>()
    val alerts: LiveData<List<AlertData>> = _alerts

    private val _typeCounts = MutableLiveData<Map<AlertType, Int>>()
    val typeCounts: LiveData<Map<AlertType, Int>> = _typeCounts

    init {
        allAlerts.observeForever { entities ->
            val alertDataList = entities.map { it.toAlertData() }
            _alerts.postValue(alertDataList)
            updateTypeCounts(alertDataList)
        }
    }

    private fun updateTypeCounts(alertList: List<AlertData>) {
        val counts = mutableMapOf<AlertType, Int>()
        AlertType.entries.forEach { type ->
            counts[type] = alertList.count { it.type == type }
        }
        _typeCounts.postValue(counts)
    }

    fun addAlert(
        type: AlertType, 
        title: String, 
        message: String, 
        priority: Priority, 
        createdBy: String = "Student", 
        timestamp: Long = System.currentTimeMillis(),
        id: String? = null // Accept optional ID
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val alertId = id ?: UUID.randomUUID().toString()
            
            // CHECK FOR DUPLICATES OR PREVIOUSLY DELETED
            val existing = repository.getAlertById(alertId)
            if (existing != null) {
                return@launch
            }

            val newAlert = AlertData(
                id = alertId,
                title = title,
                message = message,
                type = type,
                priority = priority,
                timestamp = timestamp,
                isRead = false,
                createdBy = createdBy
            )

            repository.insert(newAlert)
        }
    }

    fun markAlertAsRead(alert: AlertData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(alert.copy(isRead = true))
        }
    }

    fun markLatestAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentAlerts = _alerts.value
            if (!currentAlerts.isNullOrEmpty()) {
                val latest = currentAlerts.first()
                repository.update(latest.copy(isRead = true))
            }
        }
    }

    fun deleteAlert(alert: AlertData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(alert)
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }
}