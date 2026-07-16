package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.theme.LokivoTheme

import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemeState
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ThemeState.initialize(this)
    MobileAds.initialize(this) {}
    enableEdgeToEdge()
    setContent {
      val isDarkTheme = when(ThemeState.themeMode) {
          ThemeMode.SYSTEM -> isSystemInDarkTheme()
          ThemeMode.LIGHT -> false
          ThemeMode.DARK -> true
      }
      LokivoTheme(darkTheme = isDarkTheme) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          // Not using innerPadding here, it will be handled by the screens
          LokivoApp()
        }
      }
    }
  }
}
