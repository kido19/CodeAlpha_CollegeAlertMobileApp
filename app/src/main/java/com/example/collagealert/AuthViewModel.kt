package com.example.collagealert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> = _userRole

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        _user.value = auth.currentUser
        if (auth.currentUser != null) {
            fetchUserRole()
        }
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                _user.value = result.user

                if (result.user != null) {
                    // Add a small delay to ensure database is ready
                    delay(500)
                    fetchUserRole()
                } else {
                    _error.value = "Login failed: No user returned"
                    _isLoading.value = false
                }
            } catch (e: FirebaseAuthException) {
                when (e.errorCode) {
                    "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_EMAIL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" -> {
                        _error.value = "Invalid email or password"
                    }
                    "ERROR_TOO_MANY_REQUESTS" -> {
                        _error.value = "Too many failed attempts. Try again later."
                    }
                    else -> {
                        _error.value = "Login failed: ${e.message}"
                    }
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun fetchUserRole() {
        val uid = auth.currentUser?.uid ?: run {
            _error.value = "No user ID found"
            _isLoading.value = false
            return
        }

        println("DEBUG - Fetching role for UID: $uid")

        database.child(uid).child("role").get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.getValue(String::class.java)
                println("DEBUG - Role fetched: $role")

                if (role != null) {
                    _userRole.value = role
                } else {
                    // Role doesn't exist, create default
                    _error.value = "Role not found for user, creating default"
                    createDefaultUser(uid)
                }
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                println("DEBUG - Failed to fetch role: ${e.message}")
                _error.value = "Failed to fetch role: ${e.message}"
                _isLoading.value = false
            }
    }

    private fun createDefaultUser(uid: String) {
        val email = auth.currentUser?.email ?: "unknown@test.com"
        val defaultUser = User(
            uid = uid,
            email = email,
            name = "New User",
            role = "student"  // Default to student
        )

        database.child(uid).setValue(defaultUser)
            .addOnSuccessListener {
                println("DEBUG - Default user created with role: student")
                _userRole.value = "student"
            }
            .addOnFailureListener { e ->
                println("DEBUG - Failed to create default user: ${e.message}")
                _error.value = "Failed to create user profile"
            }
    }

    fun signUp(email: String, password: String, name: String, role: String) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // First, check if email is already in use
                val resultMethods = auth.fetchSignInMethodsForEmail(email).await()
                if (resultMethods.signInMethods?.isNotEmpty() == true) {
                    _error.value = "Email already in use. Please login instead."
                    _isLoading.value = false
                    return@launch
                }

                // Create user in Firebase Auth
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("Failed to get user ID")

                // Create user object
                val user = User(
                    uid = uid,
                    email = email,
                    name = name,
                    role = role
                )

                // Save to Realtime Database
                database.child(uid).setValue(user).await()

                // Update LiveData
                _user.value = result.user
                _userRole.value = role
                _error.value = null

            } catch (e: FirebaseAuthException) {
                when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                        _error.value = "Email already in use. Please login instead."
                    }
                    "ERROR_WEAK_PASSWORD" -> {
                        _error.value = "Password is too weak. Use at least 6 characters."
                    }
                    "ERROR_INVALID_EMAIL" -> {
                        _error.value = "Invalid email format."
                    }
                    else -> {
                        _error.value = "Sign up failed: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Sign up failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
        _user.value = null
        _userRole.value = ""
    }

    fun clearError() {
        _error.value = null
    }
}
