package com.example.data.repository

import com.example.domain.Service
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ServiceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val servicesCollection = db.collection("services")

    suspend fun saveService(service: Service): Result<Unit> {
        return try {
            val id = service.id.ifEmpty { UUID.randomUUID().toString() }
            val newService = service.copy(id = id)
            servicesCollection.document(id).set(newService).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveServices(): List<Service> {
        return try {
            val snapshot = servicesCollection.whereEqualTo("isActive", true).get().await()
            snapshot.documents.mapNotNull { it.toObject(Service::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
