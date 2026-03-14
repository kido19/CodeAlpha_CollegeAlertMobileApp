package com.example.collagealert

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AlertType {
    EXAM, SEMINAR, HOLIDAY, NOTICE, URGENT, GENERAL
}

enum class Priority {
    HIGH, MEDIUM, NORMAL, LOW
}

data class AlertData(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val message: String = "",
    val type: AlertType = AlertType.GENERAL,
    val priority: Priority = Priority.NORMAL,
    val timestamp: Long = System.currentTimeMillis(),
    var isRead: Boolean = false,
    val createdBy: String = "System"
) {
    val formattedTime: String
        get() {
            val date = Date(timestamp)
            val format = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            return format.format(date)
        }

    val priorityEmoji: String
        get() = when (priority) {
            Priority.HIGH -> "🔴"
            Priority.MEDIUM -> "🟡"
            Priority.LOW -> "🟢"
            else -> "⚪"
        }

    val typeEmoji: String
        get() = when (type) {
            AlertType.EXAM -> "📝"
            AlertType.SEMINAR -> "🎯"
            AlertType.HOLIDAY -> "🏖️"
            AlertType.NOTICE -> "📢"
            AlertType.URGENT -> "🚨"
            AlertType.GENERAL -> "📌"
        }

    fun markAsRead() {
        isRead = true
    }
}