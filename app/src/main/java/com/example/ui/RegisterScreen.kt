package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.LokivoPrimaryButton
import com.example.ui.components.LokivoTextField
import com.example.ui.components.GoogleSignInButton

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isWorker by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val authError by viewModel.error.collectAsState()
    val error = localError ?: authError

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text("Create Account", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Join Lokivo and find what you need.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(32.dp))

            // Role Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = !isWorker,
                    onClick = { isWorker = false },
                    label = { Text("Customer", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isWorker,
                    onClick = { isWorker = true },
                    label = { Text("Worker", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            LokivoTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Full Name",
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LokivoTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = "Phone Number",
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LokivoTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LokivoTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (error != null) {
                Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            LokivoPrimaryButton(
                text = if (isLoading) "Signing Up..." else "Sign Up",
                onClick = { viewModel.register(email, password, onRegisterSuccess) },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && name.isNotBlank()
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
            
            GoogleSignInButton(
                text = "Sign Up with Google",
                onIdTokenReceived = { idToken ->
                    localError = null
                    viewModel.loginWithGoogle(idToken, { _ -> onRegisterSuccess() })
                },
                onError = { msg ->
                    localError = msg
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Log In",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onBack() }
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
