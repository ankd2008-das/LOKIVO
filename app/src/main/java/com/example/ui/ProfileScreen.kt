package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R

enum class WorkerStatus { NONE, PENDING, APPROVED, REJECTED }
enum class AppMode { CUSTOMER, PROFESSIONAL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToHelpCenter: () -> Unit,
    onNavigateToWorkerRegistration: () -> Unit,
    onNavigateToWorkerDashboard: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var workerStatus by remember { mutableStateOf(WorkerStatus.NONE) }
    var currentMode by remember { mutableStateOf(AppMode.CUSTOMER) }
    val scrollState = rememberScrollState()

    // Temporary logic for demonstration: switch status by clicking the profile picture
    val cycleStatus = {
        workerStatus = when (workerStatus) {
            WorkerStatus.NONE -> WorkerStatus.PENDING
            WorkerStatus.PENDING -> WorkerStatus.APPROVED
            WorkerStatus.APPROVED -> WorkerStatus.REJECTED
            WorkerStatus.REJECTED -> WorkerStatus.NONE
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ProfileHeader(
                currentMode = currentMode,
                workerStatus = workerStatus,
                onProfilePicClick = cycleStatus,
                onNavigateToEditProfile = onNavigateToEditProfile
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                ProfileCompletionCard(onClick = onNavigateToEditProfile)
                Spacer(modifier = Modifier.height(24.dp))

                QuickStatsRow()
                Spacer(modifier = Modifier.height(32.dp))

                CurrentModeSection(
                    workerStatus = workerStatus,
                    currentMode = currentMode,
                    onModeChanged = { currentMode = it },
                    onNavigateToWorkerRegistration = onNavigateToWorkerRegistration
                )
                Spacer(modifier = Modifier.height(32.dp))

                WorkerStatusCard(
                    workerStatus = workerStatus,
                    onNavigateToWorkerRegistration = onNavigateToWorkerRegistration,
                    onNavigateToWorkerDashboard = onNavigateToWorkerDashboard
                )
                Spacer(modifier = Modifier.height(32.dp))

                MenuSections(
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    workerStatus = workerStatus,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToHelpCenter = onNavigateToHelpCenter,
                    onNavigateToWorkerRegistration = onNavigateToWorkerRegistration,
                    onLogout = onLogout
                )

                Spacer(modifier = Modifier.height(120.dp)) // padding for bottom nav
            }
        }
    }
}

@Composable
fun ProfileHeader(
    currentMode: AppMode,
    workerStatus: WorkerStatus,
    onProfilePicClick: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(bottom = 32.dp)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeBadge(currentMode = currentMode)
                IconButton(
                    onClick = onNavigateToEditProfile,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit Profile")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val displayName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "User"
                val initials = displayName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
                val email = currentUser?.email ?: ""
                val uid = currentUser?.uid?.take(8)?.uppercase() ?: "UNKNOWN"

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .shadow(16.dp, CircleShape, spotColor = Color(0x1A000000))
                        .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onProfilePicClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentUser?.photoUrl != null) {
                        coil.compose.AsyncImage(
                            model = currentUser.photoUrl,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            initials,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verified",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        email,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "ID: $uid",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeBadge(currentMode: AppMode) {
    val isCustomer = currentMode == AppMode.CUSTOMER
    val bgColor = if (isCustomer) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
    val contentColor = if (isCustomer) Color(0xFF2E7D32) else Color(0xFF1565C0)
    val text = if (isCustomer) "Customer Mode" else "Professional Mode"
    val icon = if (isCustomer) Icons.Filled.Person else Icons.Filled.Work

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(contentColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProfileCompletionCard(onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Profile Completion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "80%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { 0.8f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Complete your profile to unlock all features and build trust.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                elevation = null
            ) {
                Text("Complete Profile")
            }
        }
    }
}

@Composable
fun QuickStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(icon = Icons.Filled.Star, value = "4.8", label = "Reviews", modifier = Modifier.weight(1f))
        QuickStatCard(icon = Icons.Filled.Favorite, value = "12", label = "Favorites", modifier = Modifier.weight(1f))
        QuickStatCard(icon = Icons.Filled.Handyman, value = "34", label = "Services", modifier = Modifier.weight(1f))
    }
}

@Composable
fun QuickStatCard(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0A000000)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CurrentModeSection(
    workerStatus: WorkerStatus,
    currentMode: AppMode,
    onModeChanged: (AppMode) -> Unit,
    onNavigateToWorkerRegistration: () -> Unit
) {
    Column {
        Text(
            "Current Experience",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (workerStatus == WorkerStatus.APPROVED) {
            // Segmented Switch
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp), spotColor = Color(0x0A000000)),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    SegmentButton(
                        text = "Customer",
                        isSelected = currentMode == AppMode.CUSTOMER,
                        onClick = { onModeChanged(AppMode.CUSTOMER) },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentButton(
                        text = "Professional",
                        isSelected = currentMode == AppMode.PROFESSIONAL,
                        onClick = { onModeChanged(AppMode.PROFESSIONAL) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // Become a Professional banner (if not approved)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToWorkerRegistration() }
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0A000000)),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.WorkOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Become a Professional", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Start earning with Lokivo today.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SegmentButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        label = "bgColor"
    )
    val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "contentColor"
    )
    val elevation by animateDpAsState(if (isSelected) 4.dp else 0.dp, label = "elevation")

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        shadowElevation = elevation
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun WorkerStatusCard(
    workerStatus: WorkerStatus,
    onNavigateToWorkerRegistration: () -> Unit,
    onNavigateToWorkerDashboard: () -> Unit
) {
    AnimatedContent(
        targetState = workerStatus,
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
        },
        label = "WorkerStatusCard"
    ) { status ->
        when (status) {
            WorkerStatus.NONE -> {
                PromoCard(
                    title = "Earn with Lokivo",
                    subtitle = "Turn your skills into income and reach thousands of nearby customers.",
                    buttonText = "Become a Professional",
                    onClick = onNavigateToWorkerRegistration
                )
            }
            WorkerStatus.PENDING -> {
                StatusCard(
                    title = "Application Under Review",
                    subtitle = "Our team is reviewing your profile. Expected approval within 24 hours.",
                    buttonText = "Track Application",
                    icon = Icons.Filled.HourglassEmpty,
                    iconTint = Color(0xFFFBC02D),
                    bgColor = Color(0xFFFFF9C4),
                    onClick = onNavigateToWorkerRegistration
                )
            }
            WorkerStatus.REJECTED -> {
                StatusCard(
                    title = "Application Rejected",
                    subtitle = "Unfortunately, your profile didn't meet our criteria. Please review and resubmit.",
                    buttonText = "Resubmit Application",
                    icon = Icons.Filled.ErrorOutline,
                    iconTint = Color(0xFFD32F2F),
                    bgColor = Color(0xFFFFEBEE),
                    onClick = onNavigateToWorkerRegistration
                )
            }
            WorkerStatus.APPROVED -> {
                StatusCard(
                    title = "Professional Verified",
                    subtitle = "Congratulations! You're now receiving customer requests.",
                    buttonText = "Manage Dashboard",
                    icon = Icons.Filled.VerifiedUser,
                    iconTint = Color(0xFF388E3C),
                    bgColor = Color(0xFFE8F5E9),
                    onClick = onNavigateToWorkerDashboard
                )
            }
        }
    }
}

@Composable
fun PromoCard(title: String, subtitle: String, buttonText: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.inversePrimary)
                )
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(buttonText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Placeholder for illustration
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    subtitle: String,
    buttonText: String,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0A000000)),
        shape = RoundedCornerShape(20.dp),
        color = bgColor
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = iconTint, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Black.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconTint,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MenuSections(

    workerStatus: WorkerStatus,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelpCenter: () -> Unit,
    onNavigateToWorkerRegistration: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Column {
        MenuSection(title = "PERSONAL") {
            ProfileMenuItem(icon = Icons.Outlined.Person, title = "Edit Profile") { onNavigateToEditProfile() }
            ProfileMenuItem(icon = Icons.Outlined.FavoriteBorder, title = "Saved Workers") {}
            ProfileMenuItem(icon = Icons.Outlined.LocationOn, title = "Addresses") {}
            ProfileMenuItem(icon = Icons.Outlined.Notifications, title = "Notifications") {}
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        MenuSection(title = "PROFESSIONAL") {
            if (workerStatus == WorkerStatus.NONE) {
                ProfileMenuItem(icon = Icons.Outlined.WorkOutline, title = "Become a Professional", onClick = onNavigateToWorkerRegistration)
            } else {
                ProfileMenuItem(icon = Icons.Outlined.Assignment, title = "Application Status") {}
                if (workerStatus == WorkerStatus.APPROVED) {
                    ProfileMenuItem(icon = Icons.Outlined.Handyman, title = "Manage Services") {}
                    ProfileMenuItem(icon = Icons.Outlined.EventAvailable, title = "Availability") {}
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        MenuSection(title = "SUPPORT") {
            ProfileMenuItem(icon = Icons.Outlined.HelpOutline, title = "Help Center", onClick = onNavigateToHelpCenter)
            ProfileMenuItem(icon = Icons.Outlined.SupportAgent, title = "Contact Support") {}
            ProfileMenuItem(icon = Icons.Outlined.ReportProblem, title = "Report Issue") {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        MenuSection(title = "LEGAL") {
            ProfileMenuItem(icon = Icons.Outlined.PrivacyTip, title = "Privacy Policy") {}
            ProfileMenuItem(icon = Icons.Outlined.Description, title = "Terms & Conditions") {}
            ProfileMenuItem(icon = Icons.Outlined.Info, title = "About Lokivo") {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        MenuSection(title = "SETTINGS") {
            ProfileMenuItem(icon = Icons.Outlined.Palette, title = "Appearance") {}
            ProfileMenuItem(icon = Icons.Outlined.Language, title = "Language") {}
            ProfileMenuItem(icon = Icons.Outlined.Security, title = "Security") {}
            ProfileMenuItem(icon = Icons.Outlined.ExitToApp, title = "Logout", isDestructive = true, onClick = onLogout)
        }
    }
}

@Composable
fun MenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0A000000)),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (!isDestructive) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
