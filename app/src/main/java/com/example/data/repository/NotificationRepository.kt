package com.example.data.repository

import com.example.domain.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationsCollection = firestore.collection("notifications")

    suspend fun createNotification(receiverUID: String, title: String, body: String): Result<Unit> {
        return try {
            val id = notificationsCollection.document().id
            val notification = Notification(
                id = id,
                title = title,
                body = body,
                receiverUID = receiverUID,
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            notificationsCollection.document(id).set(notification).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getNotificationsStream(uid: String): kotlinx.coroutines.flow.Flow<List<Notification>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("receiverUID", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notifications = snapshot.documents
                        .mapNotNull { it.toObject(Notification::class.java) }
                        .sortedByDescending { it.createdAt }
                    trySend(notifications)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            notificationsCollection.document(notificationId).update("isRead", true).await()
        } catch (e: Exception) {
            // Error handling ignored for brevity
        }
    }
}
