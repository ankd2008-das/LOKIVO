package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ui.components.LokivoPrimaryButton
import com.example.ui.components.LokivoTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerEditProfileScreen(onBack: () -> Unit) {
    var name by remember { mutableStateOf("Sanjay R.") }
    var experience by remember { mutableStateOf("5") }
    var about by remember { mutableStateOf("Experienced AC repair specialist with over 5 years of experience in the field.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Profile Photo Edit
            val context = androidx.compose.ui.platform.LocalContext.current
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .clip(CircleShape)
                    .clickable { android.widget.Toast.makeText(context, "Photo upload coming soon", android.widget.Toast.LENGTH_SHORT).show() }
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Text("S", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Upload", tint = androidx.compose.ui.graphics.Color.White)
                }
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
                value = experience,
                onValueChange = { experience = it },
                placeholder = "Years of Experience",
                leadingIcon = { Icon(Icons.Filled.Work, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            LokivoTextField(
                value = about,
                onValueChange = { about = it },
                placeholder = "About Me",
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = false
            )
            Spacer(modifier = Modifier.height(48.dp))

            LokivoPrimaryButton(text = "Save Changes", onClick = onBack)
        }
    }
}
