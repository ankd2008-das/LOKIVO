package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.LocationRepository
import com.example.domain.LocationArea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel(private val repository: LocationRepository = LocationRepository()) : ViewModel() {

    private val _locations = MutableStateFlow<List<LocationArea>>(emptyList())
    val locations: StateFlow<List<LocationArea>> = _locations.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchLocations()
    }

    private fun fetchLocations() {
        viewModelScope.launch {
            _isLoading.value = true
            val fetchedLocations = repository.getLocations()
            _locations.value = fetchedLocations
            _isLoading.value = false
        }
    }
}
