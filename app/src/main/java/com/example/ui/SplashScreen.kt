package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToWorker: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val isCheckingSession by viewModel.isCheckingSession.collectAsState()
    val autoLoginRole by viewModel.autoLoginRole.collectAsState()
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
    }
    
    LaunchedEffect(isCheckingSession, autoLoginRole) {
        if (!isCheckingSession) {
            delay(1500)
            if (autoLoginRole == null) {
                onNavigateToLogin()
            } else {
                when (autoLoginRole) {
                    "admin", "super_admin" -> onNavigateToAdmin()
                    "professional", "worker" -> onNavigateToWorker()
                    else -> onNavigateToHome()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.img_app_icon),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Lokivo",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                modifier = Modifier.alpha(alphaAnim)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Everything Local. One App.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.alpha(alphaAnim)
            )
        }
    }
}
