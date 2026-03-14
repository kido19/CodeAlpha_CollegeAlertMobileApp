package com.example.collagealert

import android.app.Application
import com.google.firebase.FirebaseApp

class CollegeAlertApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}