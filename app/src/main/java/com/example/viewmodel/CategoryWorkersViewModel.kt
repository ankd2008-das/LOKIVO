package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FirestoreService
import com.example.domain.Worker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryWorkersViewModel : ViewModel() {
    private val firestoreService = FirestoreService()

    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var workersJob: Job? = null

    fun fetchWorkersByCategory(category: String) {
        _isLoading.value = true
        workersJob?.cancel()
        workersJob = viewModelScope.launch {
            try {
                firestoreService.getWorkersStream().collectLatest { allWorkers ->
                    val filtered = allWorkers.filter { 
                        it.isVerified && it.category.equals(category, ignoreCase = true)
                    }.sortedByDescending { it.rating }
                    _workers.value = filtered
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
}
