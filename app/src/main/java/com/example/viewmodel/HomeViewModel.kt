package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CategoryRepository
import com.example.data.repository.FirestoreService
import com.example.domain.Category
import com.example.domain.Worker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val firestoreService = FirestoreService()
    private val categoryRepo = CategoryRepository()

    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var workersJob: Job? = null

    init {
        fetchData()
    }

    fun fetchData() {
        _isLoading.value = true

        workersJob?.cancel()
        workersJob = viewModelScope.launch {
            try {
                firestoreService.getWorkersStream().collectLatest { allWorkers ->
                    val topWorkers = allWorkers.filter { it.isVerified }
                        .sortedByDescending { it.rating }
                        .take(10)
                    _workers.value = topWorkers
                    if (_categories.value.isNotEmpty()) {
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }

        viewModelScope.launch {
            try {
                val fetchedCategories = categoryRepo.getCategories()
                _categories.value = fetchedCategories
                if (_workers.value.isNotEmpty()) {
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
}
