package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.StorageRepository
import com.example.data.repository.UserRepository
import com.example.domain.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditProfileViewModel : ViewModel() {
    private val userRepo = UserRepository()
    private val storageRepo = StorageRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()
    
    var name = MutableStateFlow("")
    var phone = MutableStateFlow("")
    var city = MutableStateFlow("")
    var state = MutableStateFlow("")
    var photoUrl = MutableStateFlow("")
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            try {
                val doc = db.collection("users").document(uid).get().await()
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    _userState.value = user
                    name.value = user.name
                    phone.value = user.phone
                    city.value = user.city
                    this@EditProfileViewModel.state.value = user.state
                    photoUrl.value = user.photoUrl
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _isSaving.value = true
            try {
                val url = storageRepo.uploadProfilePhoto(uid, uri)
                photoUrl.value = url
                saveChanges() // auto save
            } catch (e: Exception) {
                // error
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _isSaving.value = true
            try {
                db.collection("users").document(uid).update(
                    mapOf(
                        "name" to name.value,
                        "phone" to phone.value,
                        "city" to city.value,
                        "state" to state.value,
                        "photoUrl" to photoUrl.value
                    )
                ).await()
            } catch (e: Exception) {
                // error
            } finally {
                _isSaving.value = false
            }
        }
    }
}
