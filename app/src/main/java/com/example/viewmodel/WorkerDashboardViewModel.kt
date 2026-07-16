package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BookingRepository
import com.example.data.repository.FirestoreService
import com.example.domain.Booking
import com.example.domain.Worker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WorkerDashboardViewModel : ViewModel() {
    private val bookingRepo = BookingRepository()
    private val firestoreService = FirestoreService()
    private val auth = FirebaseAuth.getInstance()
    
    private val _workerProfile = MutableStateFlow<Worker?>(null)
    val workerProfile: StateFlow<Worker?> = _workerProfile.asStateFlow()
    
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var workerJob: Job? = null
    private var bookingsJob: Job? = null

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val uid = auth.currentUser?.uid ?: return
        
        workerJob?.cancel()
        workerJob = viewModelScope.launch {
            try {
                firestoreService.getWorkerStream(uid).collectLatest { worker ->
                    _workerProfile.value = worker
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }

        bookingsJob?.cancel()
        bookingsJob = viewModelScope.launch {
            try {
                firestoreService.getWorkerBookingsStream(uid).collectLatest { fetchedBookings ->
                    _bookings.value = fetchedBookings
                }
            } catch (e: Exception) {
                // error handling
            }
        }
    }
    
    fun updateBookingStatus(bookingId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                bookingRepo.updateBookingStatus(bookingId, newStatus)
            } catch (e: Exception) {
                // error handling
            }
        }
    }
}
