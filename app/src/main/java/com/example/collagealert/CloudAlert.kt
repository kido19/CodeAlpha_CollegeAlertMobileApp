package com.example.collagealert

data class CloudAlert(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "GENERAL",
    val priority: String = "NORMAL",
    val timestamp: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val createdByName: String = "",
    val targetAudience: String = "ALL", // "ALL", "DEPARTMENT", "SPECIFIC"
    val department: String = "",
    val readBy: List<String> = emptyList()
)