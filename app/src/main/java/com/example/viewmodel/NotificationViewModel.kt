package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.NotificationRepository
import com.example.domain.Notification
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val notificationRepo = NotificationRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotifications()
    }

    private var notifJob: kotlinx.coroutines.Job? = null

    private fun loadNotifications() {
        val uid = auth.currentUser?.uid ?: return
        
        _isLoading.value = true
        notifJob?.cancel()
        notifJob = viewModelScope.launch {
            try {
                notificationRepo.getNotificationsStream(uid).collect { list ->
                    _notifications.value = list
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                // Ignore error
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepo.markAsRead(notificationId)
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
        }
    }
}
