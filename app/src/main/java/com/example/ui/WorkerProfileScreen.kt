package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.Worker
import com.example.ui.theme.StarGold
import com.example.ui.theme.WhatsAppGreen
import com.example.viewmodel.BookingViewModel
import com.example.viewmodel.WorkerProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(
    workerId: String,
    onBack: () -> Unit,
    viewModel: WorkerProfileViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel()
) {
    val worker by viewModel.worker.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    var showReviewDialog by remember { mutableStateOf(false) }
    var showBookingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(workerId) {
        viewModel.fetchWorkerById(workerId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (worker != null) {
                Surface(
                    modifier = Modifier.shadow(24.dp, spotColor = Color(0x26000000)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.navigationBarsPadding()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Book Service", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Text("₹${worker?.hourlyRate ?: 300} / hr", style = MaterialTheme.typography.titleLarge)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Button(
                                onClick = { showBookingDialog = true },
                                modifier = Modifier.weight(1.5f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Event, contentDescription = "Book")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Book Now", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            }
                            Button(
                                onClick = {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${worker?.phone ?: ""}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            ) {
                                Icon(Icons.Filled.Call, contentDescription = "Call")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (worker == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val currentWorker = worker!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Image & Top Bar
                Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                    AsyncImage(
                        model = currentWorker.profilePhoto.ifEmpty { "https://ui-avatars.com/api/?name=${currentWorker.name}&background=random" },
                        contentDescription = "Profile Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.background(Color.White, CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { /* Share */ },
                                modifier = Modifier.background(Color.White, CircleShape)
                            ) {
                                Icon(Icons.Filled.Share, "Share", tint = Color.Black)
                            }
                            IconButton(
                                onClick = { viewModel.toggleFavorite(currentWorker.uid) },
                                modifier = Modifier.background(Color.White, CircleShape)
                            ) {
                                Icon(
                                    if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    "Favorite",
                                    tint = if (isFavorite) StarGold else Color.Black
                                )
                            }
                        }
                    }
                }
                
                // Profile Details
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(currentWorker.name, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(currentWorker.category, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(String.format("%.1f", currentWorker.rating), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(" (${currentWorker.totalReviews} reviews)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.width(24.dp))
                        
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(currentWorker.city, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text("About", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        currentWorker.about,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    com.example.ui.components.BannerAd()
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reviews", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showReviewDialog = true }) {
                            Text("Write a Review")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (reviews.isEmpty()) {
                        Text("No reviews yet.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        reviews.forEach { review ->
                            ReviewItem(
                                name = review.customerName,
                                rating = review.rating,
                                date = "Recent",
                                text = review.comment
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            if (showReviewDialog) {
                var rating by remember { mutableIntStateOf(5) }
                var comment by remember { mutableStateOf("") }
                
                AlertDialog(
                    onDismissRequest = { showReviewDialog = false },
                    title = { Text("Write a Review") },
                    text = {
                        Column {
                            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                for (i in 1..5) {
                                    IconButton(onClick = { rating = i }) {
                                        Icon(
                                            if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                            contentDescription = null,
                                            tint = StarGold,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = comment,
                                onValueChange = { comment = it },
                                label = { Text("Your Review") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.addReview(currentWorker.uid, rating, comment, "Customer")
                            showReviewDialog = false
                        }) {
                            Text("Submit")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReviewDialog = false }) { Text("Cancel") }
                    }
                )
            }
            
            if (showBookingDialog) {
                var bookingDate by remember { mutableStateOf("") }
                var bookingTime by remember { mutableStateOf("") }
                var bookingAddress by remember { mutableStateOf("") }
                
                val bookingResult by bookingViewModel.bookingResult.collectAsState()
                val isBooking by bookingViewModel.isBooking.collectAsState()
                val context = androidx.compose.ui.platform.LocalContext.current

                LaunchedEffect(bookingResult) {
                    if (bookingResult?.isSuccess == true) {
                        android.widget.Toast.makeText(context, "Booking successful!", android.widget.Toast.LENGTH_SHORT).show()
                        showBookingDialog = false
                        bookingViewModel.resetResult()
                    } else if (bookingResult?.isFailure == true) {
                        android.widget.Toast.makeText(context, "Failed: ${bookingResult?.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_SHORT).show()
                        bookingViewModel.resetResult()
                    }
                }
                
                AlertDialog(
                    onDismissRequest = { if (!isBooking) showBookingDialog = false },
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
                        TextButton(onClick = { showBookingDialog = false }, enabled = !isBooking) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
fun ReviewItem(name: String, rating: Int, date: String, text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row {
                    for (i in 1..5) {
                        Icon(
                            if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = StarGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
        }
    }
}
