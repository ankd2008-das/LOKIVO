package com.example.data.repository

import com.example.domain.Review
import com.example.domain.Worker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")
    private val workersCollection = firestore.collection("workers")

    suspend fun getReviewsForWorker(workerId: String): List<Review> {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("workerId", workerId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Review::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addReview(review: Review): Result<Unit> {
        return try {
            // Check if user already reviewed
            val existing = reviewsCollection
                .whereEqualTo("workerId", review.workerId)
                .whereEqualTo("customerId", review.customerId)
                .get().await()
            if (!existing.isEmpty) {
                return Result.failure(Exception("You have already reviewed this worker."))
            }

            // Transaction to add review and update worker rating
            val docRef = reviewsCollection.document()
            val workerRef = workersCollection.document(review.workerId)

            firestore.runTransaction { transaction ->
                val workerSnapshot = transaction.get(workerRef)
                val currentRating = workerSnapshot.getDouble("rating") ?: 0.0
                val currentReviews = workerSnapshot.getLong("totalReviews")?.toInt() ?: 0

                val newReviewsCount = currentReviews + 1
                val newRating = ((currentRating * currentReviews) + review.rating) / newReviewsCount

                transaction.set(docRef, review.copy(reviewId = docRef.id, id = docRef.id))
                transaction.update(workerRef, mapOf(
                    "rating" to newRating,
                    "totalReviews" to newReviewsCount,
                    "reviewCount" to newReviewsCount
                ))
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
