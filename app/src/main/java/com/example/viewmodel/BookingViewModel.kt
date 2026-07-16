package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BookingRepository
import com.example.domain.Booking
import com.example.domain.Worker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingViewModel : ViewModel() {
    private val bookingRepo = BookingRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _isBooking = MutableStateFlow(false)
    val isBooking: StateFlow<Boolean> = _isBooking.asStateFlow()

    private val _bookingResult = MutableStateFlow<Result<Unit>?>(null)
    val bookingResult: StateFlow<Result<Unit>?> = _bookingResult.asStateFlow()

    fun createBooking(worker: Worker, date: String, time: String, address: String) {
        viewModelScope.launch {
            _isBooking.value = true
            try {
                val user = auth.currentUser
                if (user == null) {
                    _bookingResult.value = Result.failure(Exception("User not logged in"))
                    return@launch
                }
                
                val currentTime = System.currentTimeMillis()
                val booking = Booking(
                    customerId = user.uid,
                    workerId = worker.uid,
                    customerName = user.displayName ?: "Customer",
                    workerName = worker.name,
                    customerPhone = user.phoneNumber ?: "",
                    workerPhone = worker.phone,
                    category = worker.category,
                    serviceName = worker.category, // assuming category as default
                    bookingDate = date,
                    bookingTime = time,
                    bookingStatus = "Pending",
                    location = address,
                    latitude = 0.0,
                    longitude = 0.0,
                    paymentStatus = "Pending",
                    paymentMethod = "Cash",
                    totalAmount = worker.hourlyRate,
                    customerNote = "",
                    workerNote = "",
                    createdAt = currentTime,
                    updatedAt = currentTime,
                    
                    // BACKWARD COMPATIBILITY
                    customerUID = user.uid,
                    workerUID = worker.uid,
                    address = address,
                    phone = user.phoneNumber ?: "",
                    price = worker.hourlyRate
                )
                
                val result = bookingRepo.createBooking(booking)
                _bookingResult.value = result
            } catch (e: Exception) {
                _bookingResult.value = Result.failure(e)
            } finally {
                _isBooking.value = false
            }
        }
    }
    
    fun resetResult() {
        _bookingResult.value = null
    }
}
