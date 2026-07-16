package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.Category
import com.example.domain.Worker
import java.util.Calendar

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onCategoryClick: (String) -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToWorkerRegistration: () -> Unit = {},
    onWorkerClick: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val workers by viewModel.workers.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val scrollState = rememberScrollState()

    var selectedWorkerForSheet by remember { mutableStateOf<Worker?>(null) }
    var showBookingDialogForWorker by remember { mutableStateOf<Worker?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            HomeHeader()
            
            Spacer(modifier = Modifier.height(16.dp))
            PremiumSearchBar(onNavigateToSearch = onNavigateToSearch)
            
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedHeroBanner(onNavigateToSearch = onNavigateToSearch, onNavigateToWorkerRegistration = onNavigateToWorkerRegistration)
            
            Spacer(modifier = Modifier.height(24.dp))
            RecentSearches()

            Spacer(modifier = Modifier.height(32.dp))
            PopularCategories(categories = categories, onCategoryClick = onCategoryClick)
            
            Spacer(modifier = Modifier.height(24.dp))
            com.example.ui.components.BannerAd(modifier = Modifier.padding(horizontal = 24.dp))
            
            Spacer(modifier = Modifier.height(32.dp))
            EmergencyServices(onCategoryClick = onCategoryClick)

            Spacer(modifier = Modifier.height(32.dp))
            NearbyProfessionals(
                workers = workers,
                onWorkerClick = { workerId ->
                    val worker = workers.firstOrNull { it.uid == workerId }
                    if (worker != null) {
                        selectedWorkerForSheet = worker
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
            TopRatedProfessionals(
                workers = workers,
                onWorkerClick = { workerId ->
                    val worker = workers.firstOrNull { it.uid == workerId }
                    if (worker != null) {
                        selectedWorkerForSheet = worker
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(120.dp)) // bottom nav padding
        }
    }

    if (selectedWorkerForSheet != null) {
        com.example.ui.components.WorkerDetailBottomSheet(
            worker = selectedWorkerForSheet!!,
            onDismissRequest = { selectedWorkerForSheet = null },
            onBookClick = { worker ->
                showBookingDialogForWorker = worker
            },
            onViewProfileClick = { worker ->
                onWorkerClick(worker.uid)
            }
        )
    }

    if (showBookingDialogForWorker != null) {
        val currentWorker = showBookingDialogForWorker!!
        var bookingDate by remember { mutableStateOf("") }
        var bookingTime by remember { mutableStateOf("") }
        var bookingAddress by remember { mutableStateOf("") }
        
        val bookingViewModel: com.example.viewmodel.BookingViewModel = viewModel()
        val bookingResult by bookingViewModel.bookingResult.collectAsState()
        val isBooking by bookingViewModel.isBooking.collectAsState()
        val context = androidx.compose.ui.platform.LocalContext.current

        LaunchedEffect(bookingResult) {
            if (bookingResult?.isSuccess == true) {
                android.widget.Toast.makeText(context, "Booking successful!", android.widget.Toast.LENGTH_SHORT).show()
                showBookingDialogForWorker = null
                bookingViewModel.resetResult()
            } else if (bookingResult?.isFailure == true) {
                android.widget.Toast.makeText(context, "Failed: ${bookingResult?.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_SHORT).show()
                bookingViewModel.resetResult()
            }
        }

        AlertDialog(
            onDismissRequest = { if (!isBooking) showBookingDialogForWorker = null },
            title = { Text("Book Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = bookingDate,
                        onValueChange = { bookingDate = it },
                        label = { Text("Date (e.g. 12 Oct 2026)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBooking
                    )
                    OutlinedTextField(
                        value = bookingTime,
                        onValueChange = { bookingTime = it },
                        label = { Text("Time (e.g. 10:00 AM)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBooking
                    )
                    OutlinedTextField(
                        value = bookingAddress,
                        onValueChange = { bookingAddress = it },
                        label = { Text("Service Address") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = !isBooking
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        bookingViewModel.createBooking(currentWorker, bookingDate, bookingTime, bookingAddress)
                    },
                    enabled = !isBooking && bookingDate.isNotBlank() && bookingTime.isNotBlank() && bookingAddress.isNotBlank()
                ) {
                    if (isBooking) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Confirm Booking")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingDialogForWorker = null }, enabled = !isBooking) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun HomeHeader() {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning ☀️"
        in 12..16 -> "Good Afternoon 🌤️"
        else -> "Good Evening 🌙"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val displayName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "User"
                
                Text(
                    greeting,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            val context = androidx.compose.ui.platform.LocalContext.current
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { android.widget.Toast.makeText(context, "Location picker coming soon", android.widget.Toast.LENGTH_SHORT).show() }) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Battala, Agartala, Tripura",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Change Location",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            val notifyContext = androidx.compose.ui.platform.LocalContext.current
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .clip(CircleShape)
                    .clickable { android.widget.Toast.makeText(notifyContext, "Notifications coming soon", android.widget.Toast.LENGTH_SHORT).show() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurface)
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(8.dp, CircleShape, spotColor = Color(0x1A000000))
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("AD", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PremiumSearchBar(onNavigateToSearch: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(56.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A000000))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .clickable { onNavigateToSearch() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Search services, workers or areas",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        val context = androidx.compose.ui.platform.LocalContext.current
        Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = "Voice Search",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .clickable { android.widget.Toast.makeText(context, "Voice Search coming soon", android.widget.Toast.LENGTH_SHORT).show() }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = Icons.Filled.Tune,
            contentDescription = "Filter",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .clickable { onNavigateToSearch() }
        )
    }
}

@Composable
fun AnimatedHeroBanner(onNavigateToSearch: () -> Unit = {}, onNavigateToWorkerRegistration: () -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition(label = "bannerAnimation")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingElements"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(220.dp)
            .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E3C72),
                        Color(0xFF2A5298)
                    )
                )
            )
    ) {
        // Decorative background elements
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-30).dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 20.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )
        
        // Floating Icons
        Icon(Icons.Filled.Handyman, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.align(Alignment.TopEnd).offset(x = (-40).dp, y = (20 + offsetY).dp).size(32.dp))
        Icon(Icons.Filled.CleaningServices, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-80).dp, y = (10 - offsetY).dp).size(24.dp))
        Icon(Icons.Filled.FormatPaint, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-50).dp, y = (-30 + offsetY).dp).size(28.dp))


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Verified Professionals\nNear You",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 1.1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Book trusted local workers within minutes.",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onNavigateToSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1E3C72)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    Text("Find Services", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = onNavigateToWorkerRegistration,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    Text("Join as Pro", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun RecentSearches() {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Recent Searches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val searches = listOf("AC Repair near me", "Emergency Plumber", "Home Tutor for Math")
            items(searches) { search ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(search, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null, actionText: String? = "See All", onActionClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (actionText != null) {
            Text(
                actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

@Composable
fun PopularCategories(categories: List<Category>, onCategoryClick: (String) -> Unit) {
    Column {
        SectionHeader(title = "Popular Categories", subtitle = "Most booked services around you")
        Spacer(modifier = Modifier.height(16.dp))
        
        val colors = listOf(
            Color(0xFFFFF3E0) to Color(0xFFE65100),
            Color(0xFFE3F2FD) to Color(0xFF1565C0),
            Color(0xFFE8F5E9) to Color(0xFF2E7D32),
            Color(0xFFF3E5F5) to Color(0xFF6A1B9A),
            Color(0xFFFFF8E1) to Color(0xFFFF8F00),
            Color(0xFFFFEBEE) to Color(0xFFC62828)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories.take(6).withIndex().toList()) { (index, category) ->
                val (bgColor, iconColor) = colors[index % colors.size]
                CategoryCard(category, bgColor, iconColor) { onCategoryClick(category.name) }
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, bgColor: Color, iconColor: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")
    val elevation by animateDpAsState(if (isPressed) 2.dp else 12.dp, label = "elevation")

    Column(
        modifier = Modifier
            .width(88.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(76.dp)
                .shadow(elevation, RoundedCornerShape(20.dp), spotColor = iconColor.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(20.dp),
            color = bgColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Placeholder for actual illustration, using text/icon for now
                Icon(Icons.Filled.BuildCircle, contentDescription = null, tint = iconColor, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            category.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = MaterialTheme.typography.labelMedium.lineHeight * 1.1
        )
        Text(
            "120+ pros",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmergencyServices(onCategoryClick: (String) -> Unit = {}) {
    Column {
        SectionHeader(title = "Emergency Services", subtitle = "Available within 30 mins", actionText = null)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EmergencyCard("Electrician", Icons.Filled.ElectricBolt, Color(0xFFFFF3E0), Color(0xFFF57C00), Modifier.weight(1f)) { onCategoryClick("electrician") }
            EmergencyCard("Plumber", Icons.Filled.Plumbing, Color(0xFFE1F5FE), Color(0xFF0288D1), Modifier.weight(1f)) { onCategoryClick("plumber") }
            EmergencyCard("Locksmith", Icons.Filled.Key, Color(0xFFF3E5F5), Color(0xFF7B1FA2), Modifier.weight(1f)) { onCategoryClick("locksmith") }
            val context = androidx.compose.ui.platform.LocalContext.current
            EmergencyCard("Hospital", Icons.Filled.LocalHospital, Color(0xFFFFEBEE), Color(0xFFD32F2F), Modifier.weight(1f)) { 
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=hospital"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "No maps application found", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
fun EmergencyCard(title: String, icon: ImageVector, bgColor: Color, iconColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        modifier = modifier
            .aspectRatio(0.8f)
            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = iconColor.copy(alpha = 0.2f))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun NearbyProfessionals(workers: List<Worker>, onWorkerClick: (String) -> Unit = {}) {
    Column {
        SectionHeader(title = "Nearby Professionals", subtitle = "Top-rated workers in Agartala")
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(workers) { worker ->
                PremiumWorkerCard(worker = worker, onClick = { onWorkerClick(worker.id) })
            }
        }
    }
}

@Composable
fun TopRatedProfessionals(workers: List<Worker>, onWorkerClick: (String) -> Unit = {}) {
    Column {
        SectionHeader(title = "Recommended For You", subtitle = "Based on your recent searches")
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(workers.reversed()) { worker ->
                PremiumWorkerCard(worker = worker, onClick = { onWorkerClick(worker.id) })
            }
        }
    }
}

@Composable
fun PremiumWorkerCard(worker: Worker, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.clickable { onClick() }) {
            // Header: Photo, Name, Category
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(worker.name.take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(worker.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                    }
                    Text(worker.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(worker.rating.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(" (${worker.reviews})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Starting from", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹250/hr", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Available Now", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${worker.experienceYears} yrs exp • 2.5 km away", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            // Actions
            val context = androidx.compose.ui.platform.LocalContext.current
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${worker.phone}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = "Call", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        val url = "https://api.whatsapp.com/send?phone=${worker.phone}"
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

