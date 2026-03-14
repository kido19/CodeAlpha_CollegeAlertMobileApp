package com.example.collagealert

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "New Alert", it.body ?: "")
        }

        // Also check for data payload
        remoteMessage.data.let { data ->
            // Handle data message
            val title = data["title"] ?: "New Alert"
            val message = data["message"] ?: ""
            sendNotification(title, message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your server if needed
        saveTokenToFirestore(token)
    }

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "college_alerts_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Create this icon
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "College Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for college alert notifications"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun saveTokenToFirestore(token: String) {
        // Get current user from FirebaseAuth
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users")
                .document(currentUser.uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    println("Token saved successfully")
                }
                .addOnFailureListener {
                    println("Error saving token: ${it.message}")
                }
        }
    }
}