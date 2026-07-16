package com.example.data.repository

import com.example.domain.Favorite
import com.example.domain.Worker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoriteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val favoritesCollection = firestore.collection("favorites")
    private val workersCollection = firestore.collection("workers")

    suspend fun getFavorites(customerId: String): List<Worker> {
        return try {
            val snapshot = favoritesCollection.whereEqualTo("customerId", customerId).get().await()
            val workerIds = snapshot.documents.mapNotNull { it.getString("workerId") }
            
            if (workerIds.isEmpty()) return emptyList()
            
            // Note: In a real large scale app we would partition in chunks of 10 for 'in' query
            val workers = mutableListOf<Worker>()
            for (chunk in workerIds.chunked(10)) {
                val workersSnap = workersCollection.whereIn("uid", chunk).get().await()
                workers.addAll(workersSnap.documents.mapNotNull { it.toObject(Worker::class.java)?.copy(uid = it.id) })
            }
            workers
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun isFavorite(customerId: String, workerId: String): Boolean {
        return try {
            val id = "${customerId}_${workerId}"
            val doc = favoritesCollection.document(id).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleFavorite(customerId: String, workerId: String): Result<Boolean> {
        return try {
            val id = "${customerId}_${workerId}"
            val docRef = favoritesCollection.document(id)
            val doc = docRef.get().await()
            
            if (doc.exists()) {
                docRef.delete().await()
                Result.success(false) // Removed
            } else {
                val favorite = Favorite(
                    id = id,
                    customerUID = customerId,
                    workerUID = workerId,
                    customerId = customerId,
                    workerId = workerId
                )
                docRef.set(favorite).await()
                Result.success(true) // Added
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
