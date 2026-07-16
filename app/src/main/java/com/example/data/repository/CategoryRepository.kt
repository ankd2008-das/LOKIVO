package com.example.data.repository

import com.example.domain.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val categoriesCollection = firestore.collection("categories")

    suspend fun getCategories(): List<Category> {
        return try {
            val snapshot = categoriesCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                Category(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    iconName = doc.getString("iconName") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveCategory(category: Category): Result<Unit> {
        return try {
            val data = mapOf(
                "name" to category.name,
                "iconName" to category.iconName
            )
            categoriesCollection.document(category.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
