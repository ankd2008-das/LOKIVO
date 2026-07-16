package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

object ThemeState {
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        val prefs = context.applicationContext.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val savedModeStr = prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
        themeMode = try {
            ThemeMode.valueOf(savedModeStr ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        isInitialized = true
    }

    fun updateThemeMode(context: Context, mode: ThemeMode) {
        themeMode = mode
        val prefs = context.applicationContext.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("theme_mode", mode.name).apply()
    }
}

