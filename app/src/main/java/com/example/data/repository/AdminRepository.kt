package com.example.data.repository

import com.example.domain.Category
import com.example.domain.User
import com.example.domain.Worker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val workersCollection = firestore.collection("workers")
    private val categoriesCollection = firestore.collection("categories")
    private val usersCollection = firestore.collection("users")

    suspend fun getPendingWorkers(): List<Worker> {
        return try {
            val snapshot = workersCollection.whereEqualTo("approvalStatus", "pending").get().await()
            snapshot.documents.mapNotNull { it.toObject(Worker::class.java)?.copy(uid = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWorkersByStatus(status: String): List<Worker> {
        return try {
            val snapshot = workersCollection.whereEqualTo("approvalStatus", status).get().await()
            snapshot.documents.mapNotNull { it.toObject(Worker::class.java)?.copy(uid = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBlockedWorkers(): List<Worker> {
        return try {
            val snapshot = workersCollection.whereEqualTo("isBlocked", true).get().await()
            snapshot.documents.mapNotNull { it.toObject(Worker::class.java)?.copy(uid = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateWorkerStatus(workerId: String, status: String, isApproved: Boolean): Result<Unit> {
        return try {
            workersCollection.document(workerId).update(
                mapOf(
                    "approvalStatus" to status,
                    "isVerified" to isApproved
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun blockWorker(workerId: String, isBlocked: Boolean): Result<Unit> {
        return try {
            workersCollection.document(workerId).update("isBlocked", isBlocked).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllCustomers(): List<User> {
        return try {
            val snapshot = usersCollection.whereEqualTo("role", "customer").get().await()
            snapshot.documents.mapNotNull { it.toObject(User::class.java)?.copy(uid = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveCategory(category: Category): Result<Unit> {
        return try {
            categoriesCollection.document(category.id).set(category).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hideCategory(categoryId: String, isHidden: Boolean): Result<Unit> {
        return try {
            categoriesCollection.document(categoryId).update("isHidden", isHidden).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            categoriesCollection.document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnalytics(): Map<String, Any> {
        return try {
            val totalUsers = usersCollection.count().get(com.google.firebase.firestore.AggregateSource.SERVER).await().count
            val totalWorkers = workersCollection.count().get(com.google.firebase.firestore.AggregateSource.SERVER).await().count
            val approvedWorkers = workersCollection.whereEqualTo("approvalStatus", "approved").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await().count
            val pendingWorkers = workersCollection.whereEqualTo("approvalStatus", "pending").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await().count
            val totalCategories = categoriesCollection.count().get(com.google.firebase.firestore.AggregateSource.SERVER).await().count
            
            val totalFavorites = firestore.collection("favorites").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await().count
            val totalReviews = firestore.collection("reviews").count().get(com.google.firebase.firestore.AggregateSource.SERVER).await().count
            
            mapOf(
                "totalUsers" to totalUsers,
                "totalWorkers" to totalWorkers,
                "approvedWorkers" to approvedWorkers,
                "pendingWorkers" to pendingWorkers,
                "totalCategories" to totalCategories,
                "totalFavorites" to totalFavorites,
                "totalReviews" to totalReviews
            )
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
