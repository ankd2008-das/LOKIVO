package com.example.data.repository

import com.example.domain.LocationArea
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LocationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val locationsCollection = firestore.collection("locations")

    suspend fun getLocations(): List<LocationArea> {
        return try {
            val snapshot = locationsCollection.whereEqualTo("active", true).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(LocationArea::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
