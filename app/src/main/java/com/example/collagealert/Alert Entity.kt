package com.example.collagealert

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey
    val id: String, 
    val title: String,
    val message: String,
    val type: String,
    val priority: String,
    val timestamp: Long,
    val isRead: Boolean,
    val createdBy: String,
    val isDeleted: Boolean = false // New field to track soft deletes
) {
    fun toAlertData(): AlertData {
        return AlertData(
            id = id,
            title = title,
            message = message,
            type = AlertType.valueOf(type),
            priority = Priority.valueOf(priority),
            timestamp = timestamp,
            isRead = isRead,
            createdBy = createdBy
        )
    }

    companion object {
        fun fromAlertData(alert: AlertData, isDeleted: Boolean = false): AlertEntity {
            return AlertEntity(
                id = alert.id,
                title = alert.title,
                message = alert.message,
                type = alert.type.name,
                priority = alert.priority.name,
                timestamp = alert.timestamp,
                isRead = alert.isRead,
                createdBy = alert.createdBy,
                isDeleted = isDeleted
            )
        }
    }
}