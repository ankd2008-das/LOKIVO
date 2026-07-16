package com.example.ui
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.WorkerDashboardViewModel
import coil.compose.AsyncImage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.theme.StarGold

sealed class WorkerBottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Dashboard : WorkerBottomNavItem("worker_dashboard", Icons.Filled.Dashboard, "Dashboard")
    object Performance : WorkerBottomNavItem("worker_performance", Icons.Filled.TrendingUp, "Performance")
    object Profile : WorkerBottomNavItem("worker_profile", Icons.Filled.Person, "Profile")
}

@Composable
fun WorkerMainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToCustomerMode: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        WorkerBottomNavItem.Dashboard,
        WorkerBottomNavItem.Performance,
        WorkerBottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = WorkerBottomNavItem.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(WorkerBottomNavItem.Dashboard.route) {
                WorkerDashboardScreen()
            }
            composable(WorkerBottomNavItem.Performance.route) {
                WorkerPerformanceScreen()
            }
            composable(WorkerBottomNavItem.Profile.route) {
                WorkerMyProfileScreen(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToSupport = onNavigateToSupport,
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToReviews = onNavigateToReviews,
                    onNavigateToCustomerMode = onNavigateToCustomerMode,
                    onLogout = onLogout
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(viewModel: WorkerDashboardViewModel = viewModel()) {
    var isAvailable by remember { mutableStateOf(true) }
    
    val workerProfile by viewModel.workerProfile.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    if (isLoading && workerProfile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Welcome Back,", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(workerProfile?.name ?: "Professional", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!workerProfile?.profilePhoto.isNullOrEmpty()) {
                    AsyncImage(
                        model = workerProfile?.profilePhoto,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Text(workerProfile?.name?.take(1) ?: "W", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Availability Toggle
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = if (isAvailable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Current Status", style = MaterialTheme.typography.labelLarge, color = if (isAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (isAvailable) "Available for Work" else "Offline",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = isAvailable,
                    onCheckedChange = { isAvailable = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Today's Overview", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val completedJobs = bookings.count { it.status == "Completed" }
            val totalEarnings = bookings.filter { it.status == "Completed" }.sumOf { it.price }
            
            StatCard(
                title = "Earnings",
                value = "₹$totalEarnings",
                icon = Icons.Filled.TrendingUp,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Jobs Done",
                value = completedJobs.toString(),
                icon = Icons.Filled.Assignment,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Recent Requests", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(16.dp))

        // Pending Request Cards
        val pendingBookings = bookings.filter { it.status == "Pending" || it.status == "Accepted" }
        if (pendingBookings.isEmpty()) {
            Text("No new requests at the moment.", modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            pendingBookings.forEach { booking ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A000000)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(booking.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(booking.status, style = MaterialTheme.typography.labelMedium, color = if (booking.status == "Accepted") Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Requested by ${booking.customerName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${booking.bookingDate} at ${booking.bookingTime}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(booking.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        if (booking.status == "Pending") {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { 
                                    viewModel.updateBookingStatus(booking.bookingId, "Rejected")
                                    android.widget.Toast.makeText(context, "Request Declined", android.widget.Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                    Text("Decline")
                                }
                                Button(onClick = { 
                                    viewModel.updateBookingStatus(booking.bookingId, "Accepted")
                                    android.widget.Toast.makeText(context, "Request Accepted", android.widget.Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.weight(1f)) {
                                    Text("Accept")
                                }
                            }
                        } else if (booking.status == "Accepted") {
                            Button(onClick = { 
                                viewModel.updateBookingStatus(booking.bookingId, "Completed")
                                android.widget.Toast.makeText(context, "Marked as Completed", android.widget.Toast.LENGTH_SHORT).show()
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("Mark as Completed")
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0A000000))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WorkerPerformanceScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        Text("Performance Analytics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // Chart Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x0A000000))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Analytics, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Monthly Earnings Chart", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Coming Soon in Pro Version", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Rating Overview", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("4.8", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = StarGold)
                Row {
                    repeat(5) { Icon(Icons.Filled.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(16.dp)) }
                }
                Text("124 Reviews", style = MaterialTheme.typography.labelMedium)
            }
            Column {
                listOf(5 to 0.8f, 4 to 0.15f, 3 to 0.05f, 2 to 0f, 1 to 0f).forEach { (stars, progress) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$stars", style = MaterialTheme.typography.labelMedium)
                        Icon(Icons.Filled.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.width(100.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = StarGold,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun WorkerMyProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToCustomerMode: () -> Unit,
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("S", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sanjay R.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("AC Repair Specialist", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            com.example.ui.ProfileMenuItem(icon = Icons.Filled.Person, title = "Edit Profile", onClick = onNavigateToEditProfile)
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.ProfileMenuItem(icon = Icons.Filled.Star, title = "My Reviews", onClick = onNavigateToReviews)
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.ProfileMenuItem(icon = Icons.Filled.Analytics, title = "Settings", onClick = onNavigateToSettings)
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.ProfileMenuItem(icon = Icons.Filled.Person, title = "Support", onClick = onNavigateToSupport)
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.ProfileMenuItem(icon = Icons.Filled.SwapHoriz, title = "Switch to Customer Mode", onClick = onNavigateToCustomerMode)
            Spacer(modifier = Modifier.height(16.dp))
            com.example.ui.ProfileMenuItem(icon = Icons.Filled.ExitToApp, title = "Logout", isDestructive = true, onClick = onLogout)
        }
    }
}
