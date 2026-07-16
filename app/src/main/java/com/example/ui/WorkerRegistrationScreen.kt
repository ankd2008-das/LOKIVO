package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.LokivoPrimaryButton
import com.example.ui.components.LokivoTextField
import com.example.ui.components.LokivoSecondaryButton
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.WorkerRegistrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRegistrationScreen(
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    viewModel: WorkerRegistrationViewModel = viewModel()
) {
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 6
    
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submitResult by viewModel.submitResult.collectAsState()
    
    LaunchedEffect(submitResult) {
        if (submitResult?.isSuccess == true) {
            onSubmit()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 1) currentStep-- else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                if (currentStep < totalSteps) {
                    LokivoPrimaryButton(text = "Next Step", onClick = { currentStep++ })
                } else {
                    LokivoPrimaryButton(text = if (isSubmitting) "Submitting..." else "Submit Profile", onClick = { viewModel.submitWorker() }, enabled = !isSubmitting)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Progress Indicator
            Text(
                "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "step_transition"
            ) { step ->
                when (step) {
                    1 -> Step1BasicInfo(viewModel)
                    2 -> Step2Categories(viewModel)
                    3 -> Step3Location(viewModel)
                    4 -> Step4Details(viewModel)
                    5 -> Step5Verification(viewModel)
                    6 -> Step6Review(viewModel)
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun Step1BasicInfo(viewModel: WorkerRegistrationViewModel) {
    val name by viewModel.name.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val profilePhotoUri by viewModel.profilePhotoUri.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.profilePhotoUri.value = uri
    }

    Column {
        Text("Basic Information", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Let's get to know you better.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .clip(CircleShape)
                .clickable { launcher.launch("image/*") }
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            if (profilePhotoUri != null) {
                AsyncImage(
                    model = profilePhotoUri,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Upload Photo", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Upload Photo", style = MaterialTheme.typography.labelMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LokivoTextField(value = name, onValueChange = { viewModel.name.value = it }, placeholder = "Full Name")
        Spacer(modifier = Modifier.height(16.dp))
        LokivoTextField(value = phone, onValueChange = { viewModel.phone.value = it }, placeholder = "Phone Number")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step2Categories(viewModel: WorkerRegistrationViewModel) {
    val categoriesList = listOf("Electrician", "Plumber", "Carpenter", "Painter", "AC Repair", "Mechanic", "Tutor", "Cleaning", "Mobile Repair", "Appliance Repair")
    val selected by viewModel.categories.collectAsState()

    Column {
        Text("Choose Services", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("What services do you offer?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoriesList.forEach { category ->
                FilterChip(
                    selected = selected.contains(category),
                    onClick = {
                        if (selected.contains(category)) viewModel.categories.value = selected - category
                        else viewModel.categories.value = selected + category
                    },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
fun Step3Location(viewModel: WorkerRegistrationViewModel) {
    val state by viewModel.state.collectAsState()
    val city by viewModel.city.collectAsState()
    val area by viewModel.area.collectAsState()
    val landmark by viewModel.landmark.collectAsState()

    Column {
        Text("Work Location", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Where do you provide your services?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LokivoTextField(value = state, onValueChange = { viewModel.state.value = it }, placeholder = "State")
        Spacer(modifier = Modifier.height(16.dp))
        LokivoTextField(value = city, onValueChange = { viewModel.city.value = it }, placeholder = "City")
        Spacer(modifier = Modifier.height(16.dp))
        LokivoTextField(value = area, onValueChange = { viewModel.area.value = it }, placeholder = "Area / Locality")
        Spacer(modifier = Modifier.height(16.dp))
        LokivoTextField(value = landmark, onValueChange = { viewModel.landmark.value = it }, placeholder = "Landmark (Optional)")
    }
}

@Composable
fun Step4Details(viewModel: WorkerRegistrationViewModel) {
    val experience by viewModel.experience.collectAsState()
    val languages by viewModel.languages.collectAsState()
    val description by viewModel.description.collectAsState()
    val hourlyRate by viewModel.hourlyRate.collectAsState()
    val isAvailable by viewModel.isAvailable.collectAsState()
    val galleryImagesUris by viewModel.galleryImagesUris.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.galleryImagesUris.value = uris
    }

    Column {
        Text("Professional Details", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Tell customers about your expertise.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LokivoTextField(value = experience, onValueChange = { viewModel.experience.value = it }, placeholder = "Years of Experience (e.g., 5)")
        Spacer(modifier = Modifier.height(16.dp))
        LokivoTextField(value = languages, onValueChange = { viewModel.languages.value = it }, placeholder = "Languages (e.g., English, Hindi)")
        Spacer(modifier = Modifier.height(16.dp))
        LokivoTextField(value = hourlyRate, onValueChange = { viewModel.hourlyRate.value = it }, placeholder = "Hourly Rate (₹)")
        Spacer(modifier = Modifier.height(16.dp))
        LokivoTextField(value = description, onValueChange = { viewModel.description.value = it }, placeholder = "Short Description", singleLine = false)
        
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .clickable { galleryLauncher.launch("image/*") }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Upload Gallery Photos", style = MaterialTheme.typography.titleMedium)
                Text("${galleryImagesUris.size} selected", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Availability Status", style = MaterialTheme.typography.titleMedium)
                Text("Are you available to take jobs now?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = isAvailable, onCheckedChange = { viewModel.isAvailable.value = it })
        }
    }
}

@Composable
fun Step5Verification(viewModel: WorkerRegistrationViewModel) {
    val accepted by viewModel.accepted.collectAsState()
    val aadhaarFrontUri by viewModel.aadhaarFrontUri.collectAsState()
    val aadhaarBackUri by viewModel.aadhaarBackUri.collectAsState()

    val frontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> viewModel.aadhaarFrontUri.value = uri }

    val backLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> viewModel.aadhaarBackUri.value = uri }

    Column {
        Text("Verification", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Help us build a trusted community.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Phone Verified", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .clickable { frontLauncher.launch("image/*") }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Upload Aadhaar Front", style = MaterialTheme.typography.titleMedium)
                Text(if (aadhaarFrontUri != null) "Selected" else "Required", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .clickable { backLauncher.launch("image/*") }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Upload Aadhaar Back", style = MaterialTheme.typography.titleMedium)
                Text(if (aadhaarBackUri != null) "Selected" else "Required", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = accepted, onCheckedChange = { viewModel.accepted.value = it })
            Text("I accept the Terms and Conditions", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun Step6Review(viewModel: WorkerRegistrationViewModel) {
    val name by viewModel.name.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val city by viewModel.city.collectAsState()
    val area by viewModel.area.collectAsState()
    val experience by viewModel.experience.collectAsState()

    Column {
        Text("Review Summary", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Please check your details before submitting.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ReviewItem("Name", name.ifEmpty { "Not provided" })
                ReviewItem("Phone", phone.ifEmpty { "Not provided" })
                ReviewItem("Services", categories.joinToString(", ").ifEmpty { "None selected" })
                ReviewItem("Location", "$area, $city")
                ReviewItem("Experience", "$experience Years")
            }
        }
    }
}

@Composable
fun ReviewItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
