package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.Booking
import com.example.viewmodel.MyBookingsViewModel

@Composable
fun MyBookingsScreen(viewModel: MyBookingsViewModel = viewModel()) {
    val customerBookings by viewModel.customerBookings.collectAsState()
    val workerBookings by viewModel.workerBookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Active", "Completed", "Cancelled")
    
    var showReviewDialog by remember { mutableStateOf<Booking?>(null) }

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
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                "My Bookings",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { 
                        Text(
                            title, 
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal 
                        ) 
                    }
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val filterActive = { b: Booking -> b.status == "Pending" || b.status == "Accepted" }
            val filterCompleted = { b: Booking -> b.status == "Completed" }
            val filterCancelled = { b: Booking -> b.status == "Cancelled" || b.status == "Declined" }

            val currentFilter = when (selectedTabIndex) {
                0 -> filterActive
                1 -> filterCompleted
                else -> filterCancelled
            }

            val filteredCustomerBookings = customerBookings.filter(currentFilter)
            val filteredWorkerBookings = workerBookings.filter(currentFilter)

            if (filteredCustomerBookings.isEmpty() && filteredWorkerBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No bookings found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (filteredCustomerBookings.isNotEmpty()) {
                        item {
                            Text("As Customer", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        items(filteredCustomerBookings) { booking ->
                            BookingCard(booking = booking, isCustomerView = true, onLeaveReviewClick = { showReviewDialog = booking })
                        }
                    }

                    if (filteredWorkerBookings.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("As Professional", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        items(filteredWorkerBookings) { booking ->
                            BookingCard(booking = booking, isCustomerView = false)
                        }
                    }
                }
            }
        }
    }
    
    showReviewDialog?.let { booking ->
        ReviewDialog(
            booking = booking,
            onDismiss = { showReviewDialog = null },
            onSubmit = { rating, comment ->
                viewModel.submitReview(booking, rating, comment) {
                    showReviewDialog = null
                }
            }
        )
    }
}

@Composable
fun BookingCard(booking: Booking, isCustomerView: Boolean, onLeaveReviewClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A000000)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = booking.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val statusColor = when (booking.status) {
                    "Accepted", "Completed" -> Color(0xFF4CAF50)
                    "Cancelled", "Declined" -> Color.Red
                    else -> MaterialTheme.colorScheme.primary
                }
                Text(
                    text = booking.status,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isCustomerView) {
                Text("Professional: ${booking.workerName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("Customer: ${booking.customerName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Text("Date: ${booking.bookingDate} at ${booking.bookingTime}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Address: ${booking.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("₹${booking.price}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                
                if (isCustomerView && booking.status == "Completed") {
                    OutlinedButton(onClick = onLeaveReviewClick) {
                        Text("Leave Review")
                    }
                }
            }
        }
    }
}
