package com.example.data.repository

import com.example.domain.Booking
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val bookingsCollection = db.collection("bookings")
    private val notificationRepo = NotificationRepository()

    suspend fun createBooking(booking: Booking): Result<Unit> {
        return try {
            val id = booking.bookingId.ifEmpty { java.util.UUID.randomUUID().toString() }
            val newBooking = booking.copy(bookingId = id)
            bookingsCollection.document(id).set(newBooking).await()
            
            // Automatically create a notification for the worker
            notificationRepo.createNotification(
                receiverUID = booking.workerUID,
                title = "New Booking Request",
                body = "${booking.customerName} requested ${booking.category} service on ${booking.bookingDate}."
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCustomerBookings(customerUID: String): List<Booking> {
        return try {
            val snapshot = bookingsCollection.whereEqualTo("customerUID", customerUID).get().await()
            snapshot.documents.mapNotNull { it.toObject(Booking::class.java) }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getWorkerBookings(workerUID: String): List<Booking> {
        return try {
            val snapshot = bookingsCollection.whereEqualTo("workerUID", workerUID).get().await()
            snapshot.documents.mapNotNull { it.toObject(Booking::class.java) }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> {
        return try {
            bookingsCollection.document(bookingId).update("status", status).await()
            
            // Get booking details to send notification to customer
            val snapshot = bookingsCollection.document(bookingId).get().await()
            val booking = snapshot.toObject(Booking::class.java)
            if (booking != null) {
                notificationRepo.createNotification(
                    receiverUID = booking.customerUID,
                    title = "Booking Update",
                    body = "Your booking for ${booking.category} is now $status."
                )
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
