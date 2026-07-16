package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.FirestoreService
import com.example.domain.Review
import com.example.domain.Worker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job

class WorkerProfileViewModel : ViewModel() {
    private val favoriteRepo = FavoriteRepository()
    private val firestoreService = FirestoreService()
    private val auth = FirebaseAuth.getInstance()

    private val _worker = MutableStateFlow<Worker?>(null)
    val worker: StateFlow<Worker?> = _worker.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var workerJob: Job? = null
    private var reviewsJob: Job? = null

    fun fetchWorkerById(id: String) {
        _isLoading.value = true
        
        workerJob?.cancel()
        workerJob = viewModelScope.launch {
            try {
                firestoreService.getWorkerStream(id).collectLatest { fetchedWorker ->
                    _worker.value = fetchedWorker
                    val currentUser = auth.currentUser
                    if (currentUser != null && fetchedWorker != null) {
                        _isFavorite.value = favoriteRepo.isFavorite(currentUser.uid, id)
                    }
                    if (_isLoading.value) _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }

        reviewsJob?.cancel()
        reviewsJob = viewModelScope.launch {
            try {
                firestoreService.getWorkerReviewsStream(id).collectLatest { fetchedReviews ->
                    _reviews.value = fetchedReviews
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun toggleFavorite(workerId: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            val result = favoriteRepo.toggleFavorite(currentUser.uid, workerId)
            if (result.isSuccess) {
                _isFavorite.value = result.getOrNull() ?: false
            }
        }
    }

    fun addReview(workerId: String, rating: Int, comment: String, customerName: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            val review = Review(
                workerId = workerId,
                customerId = currentUser.uid,
                customerName = customerName,
                customerPhotoUrl = null,
                rating = rating,
                comment = comment
            )
            val result = firestoreService.addReviewAndUpdateRating(review)
            if (!result.isSuccess) {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }
}
