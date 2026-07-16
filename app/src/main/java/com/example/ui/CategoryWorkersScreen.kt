package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.Worker
import com.example.ui.theme.StarGold

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.CategoryWorkersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryWorkersScreen(
    categoryName: String,
    onBack: () -> Unit,
    onWorkerClick: (String) -> Unit,
    viewModel: CategoryWorkersViewModel = viewModel()
) {
    LaunchedEffect(categoryName) {
        viewModel.fetchWorkersByCategory(categoryName)
    }

    val workers by viewModel.workers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedWorkerForSheet by remember { mutableStateOf<Worker?>(null) }
    var showBookingDialogForWorker by remember { mutableStateOf<Worker?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(categoryName, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (workers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No workers found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(workers) { worker ->
                    WorkerCard(worker = worker, onClick = { selectedWorkerForSheet = worker })
                }
            }
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
fun WorkerCard(worker: Worker, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0x14000000), ambientColor = Color(0x0A000000))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    worker.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(worker.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(worker.location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = StarGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${worker.rating} (${worker.reviews})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
