package com.example.data.repository

import com.example.domain.Booking
import com.example.domain.Review
import com.example.domain.Worker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val workersCollection = db.collection("workers")
    private val bookingsCollection = db.collection("bookings")
    private val reviewsCollection = db.collection("reviews")

    // Workers Stream
    fun getWorkersStream(): Flow<List<Worker>> = callbackFlow {
        val listener = workersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val workers = snapshot.documents.mapNotNull { it.toObject(Worker::class.java) }
                trySend(workers)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getWorkerStream(workerId: String): Flow<Worker?> = callbackFlow {
        val listener = workersCollection.document(workerId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.toObject(Worker::class.java))
            }
        }
        awaitClose { listener.remove() }
    }

    // Bookings Stream
    fun getWorkerBookingsStream(workerId: String): Flow<List<Booking>> = callbackFlow {
        val listener = bookingsCollection
            .whereEqualTo("workerUID", workerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.documents
                        .mapNotNull { it.toObject(Booking::class.java) }
                        .sortedByDescending { it.createdAt }
                    trySend(bookings)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getCustomerBookingsStream(customerUID: String): Flow<List<Booking>> = callbackFlow {
        val listener = bookingsCollection
            .whereEqualTo("customerUID", customerUID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.documents
                        .mapNotNull { it.toObject(Booking::class.java) }
                        .sortedByDescending { it.createdAt }
                    trySend(bookings)
                }
            }
        awaitClose { listener.remove() }
    }

    // Reviews Stream
    fun getWorkerReviewsStream(workerId: String): Flow<List<Review>> = callbackFlow {
        val listener = reviewsCollection
            .whereEqualTo("workerId", workerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reviews = snapshot.documents
                        .mapNotNull { it.toObject(Review::class.java) }
                        .sortedByDescending { it.createdAt }
                    trySend(reviews)
                }
            }
        awaitClose { listener.remove() }
    }

    // Atomic Review Creation and Rating Update
    suspend fun addReviewAndUpdateRating(review: Review): Result<Unit> {
        return try {
            val workerRef = workersCollection.document(review.workerId)
            val newReviewRef = reviewsCollection.document()

            db.runTransaction { transaction ->
                // 1. Read worker to get current rating and review count
                val workerSnapshot = transaction.get(workerRef)
                val currentRating = workerSnapshot.getDouble("rating") ?: 0.0
                val currentReviewCount = workerSnapshot.getLong("totalReviews")?.toInt() ?: 0

                // 2. Calculate new rating
                val newReviewCount = currentReviewCount + 1
                val newRating = ((currentRating * currentReviewCount) + review.rating) / newReviewCount

                // 3. Write new review
                val reviewWithId = review.copy(id = newReviewRef.id)
                transaction.set(newReviewRef, reviewWithId)

                // 4. Update worker
                transaction.update(workerRef, "rating", newRating)
                transaction.update(workerRef, "totalReviews", newReviewCount)
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
