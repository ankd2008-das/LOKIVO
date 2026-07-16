package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.LokivoPrimaryButton
import com.example.ui.components.LokivoSecondaryButton
import com.example.ui.components.LokivoTextField

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.AuthViewModel

import com.example.ui.components.GoogleSignInButton

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val authError by viewModel.error.collectAsState()
    val error = localError ?: authError
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(200.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(32.dp)
            ) {
                Column {
                    Text("Welcome Back", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Log in to access your account", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    LokivoTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "Email address",
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LokivoTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, contentDescription = null)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it }
                            )
                            Text("Remember me", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            "Forgot Password?",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigateToForgot() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (error != null) {
                        Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    LokivoPrimaryButton(
                        text = if (isLoading) "Logging in..." else "Login",
                        onClick = { viewModel.login(email, password, onLoginSuccess) },
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                        Text("  OR  ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    GoogleSignInButton(
                        text = "Continue with Google",
                        onIdTokenReceived = { idToken ->
                            localError = null
                            viewModel.loginWithGoogle(idToken, onLoginSuccess)
                        },
                        onError = { msg ->
                            localError = msg
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LokivoSecondaryButton(
                        text = "Continue with Phone",
                        onClick = { 
                            android.widget.Toast.makeText(context, "Phone Auth coming soon", android.widget.Toast.LENGTH_SHORT).show() 
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have an account? ", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Create one",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigateToRegister() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "By continuing, you agree to our Terms of Service and Privacy Policy.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}
