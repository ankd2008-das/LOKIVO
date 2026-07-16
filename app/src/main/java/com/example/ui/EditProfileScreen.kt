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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ui.components.LokivoPrimaryButton
import com.example.ui.components.LokivoTextField
import com.example.viewmodel.EditProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val name by viewModel.name.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val city by viewModel.city.collectAsState()
    val state by viewModel.state.collectAsState()
    val photoUrl by viewModel.photoUrl.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { 
                        // Show image picker logic here (omitted for MVP)
                        android.widget.Toast.makeText(context, "Image picker coming soon", android.widget.Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f))
                )
                Icon(Icons.Filled.CameraAlt, contentDescription = "Change Photo", tint = androidx.compose.ui.graphics.Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            LokivoTextField(
                value = name,
                onValueChange = { viewModel.name.value = it },
                placeholder = "Full Name"
            )
            Spacer(modifier = Modifier.height(16.dp))

            LokivoTextField(
                value = phone,
                onValueChange = { viewModel.phone.value = it },
                placeholder = "Phone Number"
            )
            Spacer(modifier = Modifier.height(16.dp))

            LokivoTextField(
                value = city,
                onValueChange = { viewModel.city.value = it },
                placeholder = "City"
            )
            Spacer(modifier = Modifier.height(16.dp))

            LokivoTextField(
                value = state,
                onValueChange = { viewModel.state.value = it },
                placeholder = "State"
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isSaving) {
                CircularProgressIndicator()
            } else {
                LokivoPrimaryButton(
                    text = "Save Changes",
                    onClick = {
                        viewModel.saveChanges()
                        android.widget.Toast.makeText(context, "Profile Updated", android.widget.Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                )
            }
        }
    }
}
