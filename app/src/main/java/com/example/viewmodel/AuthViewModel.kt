package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepo = AuthRepository()
    private val userRepo = UserRepository()
    private val prefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(authRepo.getCurrentUser())
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _autoLoginRole = MutableStateFlow<String?>(null)
    val autoLoginRole: StateFlow<String?> = _autoLoginRole.asStateFlow()

    private val _isCheckingSession = MutableStateFlow(true)
    val isCheckingSession: StateFlow<Boolean> = _isCheckingSession.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            _isCheckingSession.value = true
            val user = _currentUser.value
            if (user != null) {
                try {
                    val userData = userRepo.createOrUpdateUser(user)
                    _autoLoginRole.value = userData["role"] as? String ?: "customer"
                } catch (e: Exception) {
                    // Default to customer on network error
                    _autoLoginRole.value = "customer"
                }
            } else {
                _autoLoginRole.value = null
            }
            _isCheckingSession.value = false
        }
    }

    fun login(email: String, pass: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepo.signInWithEmail(email, pass)
            
            if (result.isSuccess) {
                val user = result.getOrNull()
                _currentUser.value = user
                
                var role = "customer"
                if (user != null) {
                    try {
                        val userData = userRepo.createOrUpdateUser(user)
                        role = userData["role"] as? String ?: "customer"
                    } catch (e: Exception) {
                        // default to customer
                    }
                }
                _isLoading.value = false
                onSuccess(role)
            } else {
                _isLoading.value = false
                _error.value = result.exceptionOrNull()?.message ?: "Login failed"
            }
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepo.signInWithGoogle(idToken)
            
            if (result.isSuccess) {
                val user = result.getOrNull()
                _currentUser.value = user
                
                var role = "customer"
                if (user != null) {
                    try {
                        val userData = userRepo.createOrUpdateUser(user)
                        role = userData["role"] as? String ?: "customer"
                    } catch (e: Exception) {
                        // default to customer
                    }
                }
                _isLoading.value = false
                onSuccess(role)
            } else {
                _isLoading.value = false
                _error.value = result.exceptionOrNull()?.message ?: "Google Sign-In failed"
            }
        }
    }

    fun register(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepo.signUpWithEmail(email, pass)
            _isLoading.value = false
            if (result.isSuccess) {
                val user = result.getOrNull()
                _currentUser.value = user
                
                if (user != null) {
                    try {
                        userRepo.createOrUpdateUser(user)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
                
                onSuccess()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Registration failed"
            }
        }
    }

    fun updateRole(newRole: String, accountType: String = newRole) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                try {
                    userRepo.updateRole(user.uid, newRole)
                    userRepo.updateAccountType(user.uid, accountType)
                    _autoLoginRole.value = newRole
                } catch (e: Exception) {
                    _autoLoginRole.value = newRole
                }
            } else {
                _autoLoginRole.value = newRole
            }
        }
    }
    
    fun logout(onSuccess: () -> Unit) {
        authRepo.logout()
        _currentUser.value = null
        _autoLoginRole.value = null
        onSuccess()
    }
}
