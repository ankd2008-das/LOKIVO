package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.StorageRepository
import com.example.data.repository.WorkerRepository
import com.example.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WorkerRegistrationViewModel : ViewModel() {
    private val workerRepo = WorkerRepository()
    private val userRepo = UserRepository()
    private val storageRepo = StorageRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    var name = MutableStateFlow("")
    var phone = MutableStateFlow("")
    var categories = MutableStateFlow<Set<String>>(emptySet())
    var state = MutableStateFlow("")
    var city = MutableStateFlow("")
    var area = MutableStateFlow("")
    var landmark = MutableStateFlow("")
    var experience = MutableStateFlow("")
    var languages = MutableStateFlow("")
    var description = MutableStateFlow("")
    var hourlyRate = MutableStateFlow("")
    var isAvailable = MutableStateFlow(true)
    var accepted = MutableStateFlow(false)

    var profilePhotoUri = MutableStateFlow<Uri?>(null)
    var aadhaarFrontUri = MutableStateFlow<Uri?>(null)
    var aadhaarBackUri = MutableStateFlow<Uri?>(null)
    var galleryImagesUris = MutableStateFlow<List<Uri>>(emptyList())

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitResult = MutableStateFlow<Result<Unit>?>(null)
    val submitResult: StateFlow<Result<Unit>?> = _submitResult.asStateFlow()

    init {
        // Prefill name and email if possible
        auth.currentUser?.let { user ->
            name.value = user.displayName ?: ""
            // We'll also get the user's phone if available
            phone.value = user.phoneNumber ?: ""
        }
    }

    fun submitWorker() {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val user = auth.currentUser
                if (user == null) {
                    _submitResult.value = Result.failure(Exception("Not authenticated"))
                    return@launch
                }
                
                var profilePhotoUrl = ""
                var aadhaarFrontUrl = ""
                var aadhaarBackUrl = ""
                val galleryUrls = mutableListOf<String>()

                // Upload images
                profilePhotoUri.value?.let { uri ->
                    profilePhotoUrl = storageRepo.uploadProfilePhoto(user.uid, uri)
                }
                aadhaarFrontUri.value?.let { uri ->
                    aadhaarFrontUrl = storageRepo.uploadAadhaarImage(user.uid, "front", uri)
                }
                aadhaarBackUri.value?.let { uri ->
                    aadhaarBackUrl = storageRepo.uploadAadhaarImage(user.uid, "back", uri)
                }
                for (uri in galleryImagesUris.value) {
                    val url = storageRepo.uploadWorkerImage(user.uid, uri)
                    galleryUrls.add(url)
                }

                val currentTime = System.currentTimeMillis()
                val hourlyRateParsed = hourlyRate.value.toDoubleOrNull() ?: 0.0

                // Construct data exactly as required by the prompt
                val workerData = hashMapOf<String, Any>(
                    "uid" to user.uid,
                    "name" to name.value,
                    "phone" to phone.value,
                    "email" to (user.email ?: ""),
                    "profileImage" to profilePhotoUrl,
                    "aadhaarNumber" to "",
                    "panNumber" to "",
                    "serviceCategory" to (categories.value.firstOrNull() ?: "Other"),
                    "subCategory" to categories.value.joinToString(", "),
                    "experienceYears" to (experience.value.toIntOrNull() ?: 0),
                    "hourlyRate" to hourlyRateParsed,
                    "description" to description.value,
                    "skills" to emptyList<String>(),
                    "languages" to languages.value.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    "city" to city.value,
                    "area" to area.value,
                    "address" to landmark.value,
                    "latitude" to 0.0,
                    "longitude" to 0.0,
                    "availability" to isAvailable.value,
                    "workingHours" to "",
                    "rating" to 0.0,
                    "reviewCount" to 0,
                    "completedJobs" to 0,
                    "cancelledJobs" to 0,
                    "responseTime" to "",
                    "verified" to false,
                    "approved" to false,
                    "featured" to false,
                    "premium" to false,
                    "documentsUploaded" to (profilePhotoUrl.isNotEmpty() && aadhaarFrontUrl.isNotEmpty()),
                    "createdAt" to currentTime,
                    "updatedAt" to currentTime,
                    "onlineStatus" to if (isAvailable.value) "online" else "offline",
                    "notificationToken" to "",
                    "galleryImages" to galleryUrls,
                    
                    // Backward comp fields
                    "category" to (categories.value.firstOrNull() ?: "Other"),
                    "subcategory" to categories.value.joinToString(", "),
                    "experience" to experience.value,
                    "about" to description.value,
                    "state" to state.value,
                    "profilePhoto" to profilePhotoUrl,
                    "aadhaarFront" to aadhaarFrontUrl,
                    "aadhaarBack" to aadhaarBackUrl,
                    "isVerified" to false,
                    "isAvailable" to isAvailable.value,
                    "totalReviews" to 0,
                    "approvalStatus" to "pending"
                )

                // Use update if exists, otherwise set
                val docRef = db.collection("workers").document(user.uid)
                val docSnap = docRef.get().await()
                if (docSnap.exists()) {
                    workerData.remove("createdAt") // don't overwrite
                    docRef.update(workerData).await()
                } else {
                    docRef.set(workerData).await()
                }
                
                // Automatically update role and accountType in user profile
                userRepo.updateRole(user.uid, "worker")
                userRepo.updateAccountType(user.uid, "worker")
                
                // Set the custom field requested: workerRegistered = true
                db.collection("users").document(user.uid).update(
                    mapOf(
                        "accountType" to "worker",
                        "workerRegistered" to true
                    )
                ).await()
                
                _submitResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _submitResult.value = Result.failure(e)
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
