package com.example.collagealert

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.example.collagealert.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        authViewModel.user.observe(this) { user ->
            if (user != null) {
                Log.d("SIGNUP", "✅ User created: ${user.email}")
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show()

                // ALL USERS GO TO MAINACTIVITY (students)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        authViewModel.error.observe(this) { error ->
            error?.let {
                Log.e("SIGNUP", "❌ Error: $it")
                Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
                authViewModel.clearError()
                binding.progressBar.visibility = android.view.View.GONE
                binding.signUpButton.isEnabled = true
            }
        }

        authViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            binding.signUpButton.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.signUpButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            // Validation
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call sign up with FIXED role = "student" (users are ALWAYS students)
            authViewModel.signUp(email, password, name, "student")
        }

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.themeToggle.setOnClickListener {
            toggleTheme()
        }
    }

    private fun toggleTheme() {
        val isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        recreate()
    }
}
