package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AdminRepository
import com.example.domain.Category
import com.example.domain.User
import com.example.domain.Worker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel : ViewModel() {
    private val adminRepo = AdminRepository()

    private val _pendingWorkers = MutableStateFlow<List<Worker>>(emptyList())
    val pendingWorkers: StateFlow<List<Worker>> = _pendingWorkers.asStateFlow()

    private val _approvedWorkers = MutableStateFlow<List<Worker>>(emptyList())
    val approvedWorkers: StateFlow<List<Worker>> = _approvedWorkers.asStateFlow()

    private val _rejectedWorkers = MutableStateFlow<List<Worker>>(emptyList())
    val rejectedWorkers: StateFlow<List<Worker>> = _rejectedWorkers.asStateFlow()
    
    private val _blockedWorkers = MutableStateFlow<List<Worker>>(emptyList())
    val blockedWorkers: StateFlow<List<Worker>> = _blockedWorkers.asStateFlow()

    private val _customers = MutableStateFlow<List<User>>(emptyList())
    val customers: StateFlow<List<User>> = _customers.asStateFlow()
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _analytics = MutableStateFlow<Map<String, Any>>(emptyMap())
    val analytics: StateFlow<Map<String, Any>> = _analytics.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchAdminData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _pendingWorkers.value = adminRepo.getWorkersByStatus("pending")
                _approvedWorkers.value = adminRepo.getWorkersByStatus("approved")
                _rejectedWorkers.value = adminRepo.getWorkersByStatus("rejected")
                _blockedWorkers.value = adminRepo.getBlockedWorkers()
                _customers.value = adminRepo.getAllCustomers()
                _analytics.value = adminRepo.getAnalytics()
                // Fetch categories
                val catSnapshot = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("categories").get().await()
                _categories.value = catSnapshot.documents.mapNotNull { it.toObject(Category::class.java)?.copy(id = it.id) }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveWorker(workerId: String) {
        viewModelScope.launch {
            val result = adminRepo.updateWorkerStatus(workerId, "approved", true)
            if (result.isSuccess) fetchAdminData()
        }
    }

    fun rejectWorker(workerId: String) {
        viewModelScope.launch {
            val result = adminRepo.updateWorkerStatus(workerId, "rejected", false)
            if (result.isSuccess) fetchAdminData()
        }
    }

    fun blockWorker(workerId: String, isBlocked: Boolean) {
        viewModelScope.launch {
            val result = adminRepo.blockWorker(workerId, isBlocked)
            if (result.isSuccess) fetchAdminData()
        }
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            val result = adminRepo.saveCategory(category)
            if (result.isSuccess) fetchAdminData()
        }
    }

    fun hideCategory(categoryId: String, isHidden: Boolean) {
        viewModelScope.launch {
            val result = adminRepo.hideCategory(categoryId, isHidden)
            if (result.isSuccess) fetchAdminData()
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            val result = adminRepo.deleteCategory(categoryId)
            if (result.isSuccess) fetchAdminData()
        }
    }
}
