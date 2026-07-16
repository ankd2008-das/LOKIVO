package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FavoriteRepository
import com.example.domain.Worker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    private val favoriteRepo = FavoriteRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _favorites = MutableStateFlow<List<Worker>>(emptyList())
    val favorites: StateFlow<List<Worker>> = _favorites.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchFavorites() {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val workers = favoriteRepo.getFavorites(currentUser.uid)
                _favorites.value = workers
            } catch (e: Exception) {
                // handle
            } finally {
                _isLoading.value = false
            }
        }
    }
}
