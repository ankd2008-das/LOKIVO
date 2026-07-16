package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences

class SearchHistoryRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
    private val KEY_HISTORY = "recent_searches"

    fun getSearchHistory(): List<String> {
        val historyStr = prefs.getString(KEY_HISTORY, null)
        return if (historyStr.isNullOrEmpty()) {
            emptyList()
        } else {
            historyStr.split("||").filter { it.isNotBlank() }
        }
    }

    fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        val currentHistory = getSearchHistory().toMutableList()
        // Remove if it already exists to move it to the top
        currentHistory.remove(query)
        currentHistory.add(0, query)
        
        // Keep only top 10
        if (currentHistory.size > 10) {
            currentHistory.removeLast()
        }
        
        prefs.edit().putString(KEY_HISTORY, currentHistory.joinToString("||")).apply()
    }
    
    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
