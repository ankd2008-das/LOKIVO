package com.example.domain

data class Worker(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val profileImage: String = "",
    val aadhaarNumber: String = "",
    val panNumber: String = "",
    val serviceCategory: String = "",
    val subCategory: String = "",
    val experienceYears: Int = 0,
    val hourlyRate: Double = 0.0,
    val description: String = "",
    val skills: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val city: String = "",
    val area: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val availability: Boolean = true,
    val workingHours: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val completedJobs: Int = 0,
    val cancelledJobs: Int = 0,
    val responseTime: String = "",
    val verified: Boolean = false,
    val approved: Boolean = false,
    val featured: Boolean = false,
    val premium: Boolean = false,
    val documentsUploaded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val onlineStatus: String = "offline",
    val notificationToken: String = "",
    val galleryImages: List<String> = emptyList(),
    
    // BACKWARD COMPATIBILITY (Old fields are retained here so UI doesn't crash)
    val category: String = "",
    val subcategory: String = "",
    val experience: String = "",
    val about: String = "",
    val state: String = "",
    val profilePhoto: String = "",
    val aadhaarFront: String = "",
    val aadhaarBack: String = "",
    val isVerified: Boolean = false,
    val isAvailable: Boolean = true,
    val totalReviews: Int = 0,
    val approvalStatus: String = "pending"
) {
    val id: String get() = uid
    val location: String get() = "$area, $city"
    val reviews: Int get() = if (reviewCount > 0) reviewCount else totalReviews
    val isApproved: Boolean get() = verified || isVerified || approved
    val imageUrl: String? get() = profileImage.takeIf { it.isNotEmpty() } ?: profilePhoto.takeIf { it.isNotEmpty() }
    val whatsapp: String get() = phone
    val workerStatus: String get() = if (isApproved) "approved" else approvalStatus
    val isBlocked: Boolean get() = false
}

data class Category(
    val id: String = "",
    val name: String = "",
    val iconName: String = "",
    val isHidden: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val state: String = "",
    val role: String = "customer",
    val photoUrl: String = "",
    val isVerified: Boolean = false,
    val isBlocked: Boolean = false,
    val profileCompleted: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis(),
    val notificationToken: String = "",
    val favoriteWorkers: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val language: String = "en",
    val theme: String = "system",
    // BACKWARD COMPATIBILITY
    val accountType: String = "",
    val workerRegistered: Boolean = false
)

data class Service(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val image: String = "",
    val price: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class Booking(
    val bookingId: String = "",
    val customerId: String = "",
    val workerId: String = "",
    val customerName: String = "",
    val workerName: String = "",
    val customerPhone: String = "",
    val workerPhone: String = "",
    val category: String = "",
    val serviceName: String = "",
    val bookingDate: String = "",
    val bookingTime: String = "",
    val bookingStatus: String = "Pending",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val paymentStatus: String = "Pending",
    val paymentMethod: String = "Cash",
    val totalAmount: Double = 0.0,
    val customerNote: String = "",
    val workerNote: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // BACKWARD COMPATIBILITY
    val customerUID: String = "",
    val workerUID: String = "",
    val status: String = "Pending",
    val address: String = "",
    val phone: String = "",
    val price: Double = 0.0
)

data class Review(
    val reviewId: String = "",
    val workerId: String = "",
    val customerId: String = "",
    val rating: Int = 0,
    val review: String = "",
    val images: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    
    // BACKWARD COMPATIBILITY
    val id: String = "",
    val customerName: String = "",
    val customerPhotoUrl: String? = null,
    val comment: String = ""
)

data class Favorite(
    val id: String = "",
    val customerUID: String = "",
    val workerUID: String = "",
    val customerId: String = "",
    val workerId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Notification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val receiverUID: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class LocationArea(
    val id: String = "",
    val name: String = "",
    val displayName: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "",
    val pincode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val active: Boolean = true,
    val displayOrder: Int = 0
)


