package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SearchHistoryRepository
import com.example.data.repository.FirestoreService
import com.example.domain.Worker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val searchHistoryRepo = SearchHistoryRepository(application)
    private val firestoreService = FirestoreService()
    
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<Worker>>(emptyList())
    val searchResults: StateFlow<List<Worker>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadHistory()
    }

    private fun loadHistory() {
        _searchHistory.value = searchHistoryRepo.getSearchHistory()
    }

    fun saveSearchQuery(query: String) {
        if (query.isNotBlank()) {
            searchHistoryRepo.addSearchQuery(query.trim())
            loadHistory()
        }
    }
    
    fun performSearch(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        _isSearching.value = true
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                firestoreService.getWorkersStream().collectLatest { allWorkers ->
                    val lowercaseQuery = query.lowercase()
                    val results = allWorkers.filter { worker ->
                        worker.isVerified && (
                        worker.name.lowercase().contains(lowercaseQuery) ||
                        worker.category.lowercase().contains(lowercaseQuery) ||
                        worker.subcategory.lowercase().contains(lowercaseQuery) ||
                        worker.city.lowercase().contains(lowercaseQuery) ||
                        worker.area.lowercase().contains(lowercaseQuery)
                        )
                    }
                    _searchResults.value = results
                    _isSearching.value = false
                    
                    if (results.isNotEmpty()) {
                        saveSearchQuery(query)
                    }
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
                _isSearching.value = false
            }
        }
    }
    
    fun clearHistory() {
        searchHistoryRepo.clearHistory()
        loadHistory()
    }
}
