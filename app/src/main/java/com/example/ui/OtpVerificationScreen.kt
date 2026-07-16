package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LokivoPrimaryButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    onVerifySuccess: () -> Unit,
    onBack: () -> Unit
) {
    var otpValue by remember { mutableStateOf("") }
    var timer by remember { mutableIntStateOf(30) }

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer--
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Verification Code", style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("We have sent a verification code to your email/phone.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = otpValue,
                onValueChange = { if (it.length <= 4) otpValue = it },
                modifier = Modifier
                    .width(160.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0F000000)),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                textStyle = MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center, letterSpacing = 8.sp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LokivoPrimaryButton(text = "Verify", onClick = onVerifySuccess, enabled = otpValue.length == 4)

            Spacer(modifier = Modifier.height(24.dp))
            
            if (timer > 0) {
                Text("Resend code in 00:${timer.toString().padStart(2, '0')}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                TextButton(onClick = { timer = 30 }) {
                    Text("Resend Code", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
