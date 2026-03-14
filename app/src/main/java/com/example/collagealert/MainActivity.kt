package com.example.collagealert

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collagealert.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: AlertAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        setupDatabase()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        updateUIInfo()
        updateFCMToken()
        listenForCloudAlerts()
    }

    private fun setupDatabase() {
        val db = AppDatabase.getDatabase(this)
        val repository = AlertRepository(db.alertDao())
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
    }

    private fun setupClickListeners() {
        // 🔐 FIXED LOGOUT BUTTON
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.themeToggle.setOnClickListener { toggleTheme() }

        binding.examCard.setOnClickListener {
            viewModel.addAlert(AlertType.EXAM, "Mid-term Exams", "Schedule published", Priority.HIGH, "Student")
        }

        binding.seminarCard.setOnClickListener {
            viewModel.addAlert(AlertType.SEMINAR, "AI Guest Lecture", "Mr. Solomon from AAU", Priority.MEDIUM, "Student")
        }

        binding.holidayCard.setOnClickListener {
            viewModel.addAlert(AlertType.HOLIDAY, "Easter Break", "Campus closed April 15-22", Priority.LOW, "Student")
        }

        binding.noticeCard.setOnClickListener {
            viewModel.addAlert(AlertType.NOTICE, "Library Hours", "Extended hours during exams", Priority.NORMAL, "Student")
        }

        binding.urgentCard.setOnClickListener {
            viewModel.addAlert(AlertType.URGENT, "Campus Emergency", "Evacuation drill", Priority.HIGH, "Student")
        }

        binding.markReadCard.setOnClickListener {
            viewModel.markLatestAsRead()
            Toast.makeText(this, "Latest alert marked as read", Toast.LENGTH_SHORT).show()
        }

        binding.clearButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear All")
                .setMessage("Delete all alerts?")
                .setPositiveButton("Yes") { _, _ -> viewModel.clearAllAlerts() }
                .setNegativeButton("No", null)
                .show()
        }

        binding.showStatsButton.setOnClickListener { showStatistics() }
    }

    private fun toggleTheme() {
        val mode = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun listenForCloudAlerts() {
        database.child("notices").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(RealtimeAlert::class.java)?.let {
                    val data = it.toAlertData()
                    viewModel.addAlert(data.type, data.title, data.message, data.priority, data.createdBy, it.timestamp, it.id)
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, p1: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = AlertAdapter(emptyList(), { showAlertDetails(it) }, { showAlertOptions(it) })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.alerts.observe(this) { alerts ->
            adapter.updateData(alerts)
            if (alerts.isNotEmpty()) binding.recyclerView.smoothScrollToPosition(0)
        }
        viewModel.unreadCount.observe(this) { count -> binding.statsNumber.text = count.toString() }
    }

    private fun updateUIInfo() {
        val sdf = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
        binding.dateText.text = sdf.format(Date())
        val email = auth.currentUser?.email ?: "Student"
        binding.userNameText.text = email.substringBefore("@").replaceFirstChar { it.uppercase() }
    }

    private fun updateFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                auth.currentUser?.uid?.let { uid ->
                    database.child("users").child(uid).child("fcmToken").setValue(task.result)
                }
            }
        }
    }

    private fun showAlertDetails(alert: AlertData) {
        AlertDialog.Builder(this)
            .setTitle("${alert.typeEmoji} ${alert.title}")
            .setMessage(alert.message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showAlertOptions(alert: AlertData) {
        AlertDialog.Builder(this)
            .setTitle("Options")
            .setItems(arrayOf("Mark as Read", "Delete")) { _, which ->
                if (which == 0) viewModel.markAlertAsRead(alert)
                else viewModel.deleteAlert(alert)
            }.show()
    }

    private fun showStatistics() {
        val total = viewModel.totalCount.value ?: 0
        val unread = viewModel.unreadCount.value ?: 0
        AlertDialog.Builder(this)
            .setTitle("Statistics")
            .setMessage("Total Alerts: $total\nUnread Alerts: $unread")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            navigateToLogin()
        }
    }
}