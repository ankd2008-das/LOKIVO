package com.example.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadProfilePhoto(uid: String, imageUri: Uri): String {
        val ref = storageRef.child("profile_images/$uid.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadWorkerImage(uid: String, imageUri: Uri): String {
        val imageId = UUID.randomUUID().toString()
        val ref = storageRef.child("worker_images/$uid/$imageId.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadAadhaarImage(uid: String, side: String, imageUri: Uri): String {
        val ref = storageRef.child("aadhaar/$uid/$side.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }
}
