package com.example.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun createOrUpdateUser(user: FirebaseUser, accountType: String? = null): Map<String, Any> {
        val docRef = usersCollection.document(user.uid)
        val doc = docRef.get().await()
        val role = "customer"
        val currentTime = System.currentTimeMillis()

        if (doc.exists()) {
            val updates = mutableMapOf<String, Any>(
                "lastLogin" to currentTime,
                "updatedAt" to currentTime
            )
            if (accountType != null && !doc.contains("accountType")) {
                updates["accountType"] = accountType
            }
            docRef.update(updates).await()
            return doc.data ?: emptyMap()
        } else {
            val userData = hashMapOf<String, Any>(
                "uid" to user.uid,
                "name" to (user.displayName ?: ""),
                "email" to (user.email ?: ""),
                "phone" to (user.phoneNumber ?: ""),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "role" to role,
                "city" to "",
                "state" to "",
                "address" to "",
                "latitude" to 0.0,
                "longitude" to 0.0,
                "profileCompleted" to 30, // Basic info
                "favoriteWorkers" to emptyList<String>(),
                "recentSearches" to emptyList<String>(),
                "language" to "en",
                "theme" to "system",
                "createdAt" to currentTime,
                "updatedAt" to currentTime,
                "lastLogin" to currentTime,
                "isVerified" to false,
                "isBlocked" to false,
                "notificationToken" to ""
            )
            if (accountType != null) {
                userData["accountType"] = accountType
            }
            docRef.set(userData).await()
            return userData
        }
    }

    suspend fun updateAccountType(uid: String, type: String) {
        usersCollection.document(uid).update(
            mapOf(
                "accountType" to type,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }
    
    suspend fun updateRole(uid: String, type: String) {
        usersCollection.document(uid).update(
            mapOf(
                "role" to type,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun getUserRole(uid: String): String {
        return try {
            val doc = usersCollection.document(uid).get().await()
            doc.getString("role") ?: "customer"
        } catch (e: Exception) {
            "customer"
        }
    }
}
