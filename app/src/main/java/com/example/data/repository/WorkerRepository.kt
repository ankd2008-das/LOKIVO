package com.example.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.domain.Worker
import kotlinx.coroutines.tasks.await

class WorkerRepository {
    private val db = FirebaseFirestore.getInstance()
    private val workersCollection = db.collection("workers")

    suspend fun saveWorker(worker: Worker): Result<Unit> {
        return try {
            workersCollection.document(worker.id).set(worker).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVerifiedWorkers(): List<Worker> {
        val snapshot = workersCollection.whereEqualTo("isVerified", true).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Worker::class.java)?.copy(uid = it.id) }
    }
    
    suspend fun getWorkersByCategory(category: String): List<Worker> {
        val snapshot = workersCollection.whereEqualTo("isVerified", true).whereEqualTo("category", category).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Worker::class.java)?.copy(uid = it.id) }
    }
    
    suspend fun getTopWorkers(): List<Worker> {
        val snapshot = workersCollection.whereEqualTo("isVerified", true).orderBy("rating", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(10).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Worker::class.java)?.copy(uid = it.id) }
    }
    
    suspend fun getWorker(workerId: String): Worker? {
        val doc = workersCollection.document(workerId).get().await()
        return doc.toObject(Worker::class.java)?.copy(uid = doc.id)
    }
    
    suspend fun updateWorker(workerId: String, data: Map<String, Any>) {
        workersCollection.document(workerId).update(data).await()
    }
}
