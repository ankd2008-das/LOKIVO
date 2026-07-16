package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FirestoreService
import com.example.domain.Booking
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyBookingsViewModel : ViewModel() {
    private val firestoreService = FirestoreService()
    private val auth = FirebaseAuth.getInstance()

    private val _customerBookings = MutableStateFlow<List<Booking>>(emptyList())
    val customerBookings: StateFlow<List<Booking>> = _customerBookings.asStateFlow()

    private val _workerBookings = MutableStateFlow<List<Booking>>(emptyList())
    val workerBookings: StateFlow<List<Booking>> = _workerBookings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var customerJob: Job? = null
    private var workerJob: Job? = null

    init {
        fetchBookings()
    }

    fun fetchBookings() {
        val uid = auth.currentUser?.uid ?: return
        
        _isLoading.value = true
        
        customerJob?.cancel()
        customerJob = viewModelScope.launch {
            try {
                firestoreService.getCustomerBookingsStream(uid).collectLatest { bookings ->
                    _customerBookings.value = bookings
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
        
        workerJob?.cancel()
        workerJob = viewModelScope.launch {
            try {
                firestoreService.getWorkerBookingsStream(uid).collectLatest { bookings ->
                    _workerBookings.value = bookings
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun submitReview(booking: Booking, rating: Int, comment: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val name = auth.currentUser?.displayName ?: "Customer"
        val photoUrl = auth.currentUser?.photoUrl?.toString()
        
        viewModelScope.launch {
            try {
                val review = com.example.domain.Review(
                    workerId = booking.workerUID,
                    customerId = uid,
                    customerName = name,
                    customerPhotoUrl = photoUrl,
                    rating = rating,
                    comment = comment
                )
                
                val result = firestoreService.addReviewAndUpdateRating(review)
                onResult(result.isSuccess)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
