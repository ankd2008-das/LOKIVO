package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.domain.Worker
import com.example.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onWorkerSelected: (Worker) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var selectedWorkerForSheet by remember { mutableStateOf<Worker?>(null) }
    var showBookingDialogForWorker by remember { mutableStateOf<Worker?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text(
                "Search",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A000000), ambientColor = Color(0x05000000)),
                placeholder = { Text("Search for workers, categories...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { 
                        viewModel.performSearch(searchQuery)
                    }
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (searchQuery.isEmpty() && searchHistory.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Searches",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { viewModel.clearHistory() }) {
                    Text("Clear")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(searchHistory) { historyItem ->
                    SearchChip(
                        text = historyItem,
                        onClick = {
                            searchQuery = historyItem
                            viewModel.performSearch(historyItem)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(searchResults) { worker ->
                    WorkerSearchResultItem(worker = worker, onClick = { selectedWorkerForSheet = worker })
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
                onWorkerSelected(worker)
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
        val context = LocalContext.current

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
fun SearchChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun WorkerSearchResultItem(worker: Worker, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0F000000)),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = worker.profilePhoto.ifEmpty { "https://ui-avatars.com/api/?name=${worker.name}&background=random" },
                contentDescription = worker.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(worker.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(worker.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(String.format("%.1f", worker.rating), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    Text(" (${worker.totalReviews})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("₹${worker.hourlyRate}/hr", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}
