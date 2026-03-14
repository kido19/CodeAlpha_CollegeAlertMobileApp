package com.example.collagealert

data class RealtimeAlert(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "GENERAL",
    val priority: String = "NORMAL",
    val timestamp: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val createdByName: String = "",
    val targetAudience: String = "ALL"
) {
    // Convert to your existing AlertData for local storage
    fun toAlertData(): AlertData {
        return AlertData(
            id = id, // Now using String ID directly
            title = title,
            message = message,
            type = when (type) {
                "EXAM" -> AlertType.EXAM
                "SEMINAR" -> AlertType.SEMINAR
                "HOLIDAY" -> AlertType.HOLIDAY
                "NOTICE" -> AlertType.NOTICE
                "URGENT" -> AlertType.URGENT
                else -> AlertType.GENERAL
            },
            priority = when (priority) {
                "HIGH" -> Priority.HIGH
                "MEDIUM" -> Priority.MEDIUM
                "LOW" -> Priority.LOW
                else -> Priority.NORMAL
            },
            timestamp = timestamp,
            isRead = false,
            createdBy = createdByName
        )
    }
}