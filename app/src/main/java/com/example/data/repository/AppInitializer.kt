package com.example.data.repository

import com.example.domain.Category
import com.example.domain.LocationArea
import com.example.domain.Service
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AppInitializer {
    private val db = FirebaseFirestore.getInstance()

    suspend fun initializeDataIfEmpty() {
        try {
            initializeCategories()
            initializeAreas()
            initializeServices()
            initializeAdmin()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun initializeCategories() {
        val snapshot = db.collection("categories").limit(1).get().await()
        if (snapshot.isEmpty) {
            val defaultCategories = listOf(
                Category(id = "plumber", name = "Plumbing", iconName = "water_drop"),
                Category(id = "electrician", name = "Electrician", iconName = "electric_bolt"),
                Category(id = "cleaner", name = "Cleaning", iconName = "cleaning_services"),
                Category(id = "carpenter", name = "Carpenter", iconName = "handyman"),
                Category(id = "painter", name = "Painting", iconName = "format_paint")
            )
            val batch = db.batch()
            for (category in defaultCategories) {
                val ref = db.collection("categories").document(category.id)
                batch.set(ref, category)
            }
            batch.commit().await()
        }
    }

    private suspend fun initializeAreas() {
        val snapshot = db.collection("areas").limit(1).get().await()
        if (snapshot.isEmpty) {
            val agartalaAreas = listOf(
                LocationArea(id = "gb", name = "GB Bazar", displayName = "GB Bazar", pincode = "799006", latitude = 23.856, longitude = 91.286, active = true, displayOrder = 1),
                LocationArea(id = "kunjaban", name = "Kunjaban", displayName = "Kunjaban", pincode = "799006", latitude = 23.853, longitude = 91.281, active = true, displayOrder = 2),
                LocationArea(id = "math_chowmuhani", name = "Math Chowmuhani", displayName = "Math Chowmuhani", pincode = "799001", latitude = 23.833, longitude = 91.277, active = true, displayOrder = 3),
                LocationArea(id = "radhanagar", name = "Radhanagar", displayName = "Radhanagar", pincode = "799001", latitude = 23.840, longitude = 91.278, active = true, displayOrder = 4),
                LocationArea(id = "indranagar", name = "Indranagar", displayName = "Indranagar", pincode = "799006", latitude = 23.858, longitude = 91.295, active = true, displayOrder = 5)
            )
            val batch = db.batch()
            for (area in agartalaAreas) {
                val ref = db.collection("areas").document(area.id)
                batch.set(ref, area)
            }
            batch.commit().await()
        }
    }

    private suspend fun initializeServices() {
        val snapshot = db.collection("services").limit(1).get().await()
        if (snapshot.isEmpty) {
            val defaultServices = listOf(
                Service(id = "pipe_repair", title = "Pipe Repair", category = "Plumbing", price = 300.0),
                Service(id = "wiring", title = "Home Wiring", category = "Electrician", price = 500.0),
                Service(id = "deep_clean", title = "Deep Cleaning", category = "Cleaning", price = 999.0)
            )
            val batch = db.batch()
            for (service in defaultServices) {
                val ref = db.collection("services").document(service.id)
                batch.set(ref, service)
            }
            batch.commit().await()
        }
    }
    
    private suspend fun initializeAdmin() {
        val adminDoc = db.collection("admin").document("settings").get().await()
        if (!adminDoc.exists()) {
            db.collection("admin").document("settings").set(
                mapOf(
                    "platformFee" to 5.0,
                    "maintenanceMode" to false,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()
        }
    }
}
