package com.example.collagealert

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "student", // "admin" or "student"
    val department: String = "",
    val fcmToken: String = ""
)