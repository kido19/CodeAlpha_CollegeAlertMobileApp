package com.example.collagealert

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collagealert.databinding.ActivityAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var noticeAdapter: NoticeAdapter
    private val noticesList = mutableListOf<RealtimeAlert>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        checkAdminStatus()
        setupRecyclerView()
        setupClickListeners()
        loadNotices()
    }

    private fun setupRecyclerView() {
        noticeAdapter = NoticeAdapter(
            notices = noticesList,
            onDeleteClick = { notice -> showDeleteConfirmation(notice) }
        )
        binding.noticesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.noticesRecyclerView.adapter = noticeAdapter
    }

    private fun loadNotices() {
        database.child("notices").orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    noticesList.clear()
                    for (noticeSnapshot in snapshot.children) {
                        val notice = noticeSnapshot.getValue(RealtimeAlert::class.java)
                        notice?.let { noticesList.add(0, it) }
                    }
                    noticeAdapter.updateData(noticesList)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showDeleteConfirmation(notice: RealtimeAlert) {
        AlertDialog.Builder(this)
            .setTitle("Delete Notice")
            .setMessage("Are you sure?")
            .setPositiveButton("Delete") { _, _ -> deleteNotice(notice) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteNotice(notice: RealtimeAlert) {
        database.child("notices").child(notice.id).removeValue()
            .addOnSuccessListener { Toast.makeText(this, "Notice deleted", Toast.LENGTH_SHORT).show() }
    }

    private fun setupClickListeners() {
        binding.logoutButton.setOnClickListener { showLogoutConfirmation() }
        binding.themeToggle.setOnClickListener { toggleTheme() }
        binding.sendAlertButton.setOnClickListener { sendAlert() }
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
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                navigateToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkAdminStatus() {
        val uid = auth.currentUser?.uid ?: return navigateToLogin()
        database.child("users").child(uid).child("role").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.getValue(String::class.java) != "admin") {
                    navigateToMain()
                }
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun sendAlert() {
        val title = binding.titleEditText.text.toString().trim()
        val message = binding.messageEditText.text.toString().trim()

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val type = when (binding.typeChipGroup.checkedChipId) {
            R.id.examChip -> "EXAM"
            R.id.seminarChip -> "SEMINAR"
            R.id.holidayChip -> "HOLIDAY"
            R.id.noticeChip -> "NOTICE"
            R.id.urgentChip -> "URGENT"
            else -> "GENERAL"
        }

        val priority = when (binding.priorityRadioGroup.checkedRadioButtonId) {
            R.id.highPriority -> "HIGH"
            R.id.mediumPriority -> "MEDIUM"
            else -> "LOW"
        }

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.sendAlertButton.isEnabled = false

        val alertId = database.child("notices").push().key ?: return
        val alert = RealtimeAlert(alertId, title, message, type, priority, System.currentTimeMillis(), auth.currentUser?.uid ?: "", "Admin")

        database.child("notices").child(alertId).setValue(alert)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Alert Sent!", Toast.LENGTH_SHORT).show()
                binding.titleEditText.text?.clear()
                binding.messageEditText.text?.clear()
                binding.progressBar.visibility = android.view.View.GONE
                binding.sendAlertButton.isEnabled = true
            }
            .addOnFailureListener {
                binding.progressBar.visibility = android.view.View.GONE
                binding.sendAlertButton.isEnabled = true
            }
    }
}