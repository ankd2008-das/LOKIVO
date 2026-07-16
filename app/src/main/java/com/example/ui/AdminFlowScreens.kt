package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.User
import com.example.domain.Worker
import com.example.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Workers", "Customers", "Categories", "Analytics")

    LaunchedEffect(Unit) {
        viewModel.fetchAdminData()
    }

    val pendingWorkers by viewModel.pendingWorkers.collectAsState()
    val approvedWorkers by viewModel.approvedWorkers.collectAsState()
    val rejectedWorkers by viewModel.rejectedWorkers.collectAsState()
    val blockedWorkers by viewModel.blockedWorkers.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val analytics by viewModel.analytics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> DashboardTab(
                        analytics = analytics,
                        pendingWorkers = pendingWorkers,
                        onApprove = { viewModel.approveWorker(it) },
                        onReject = { viewModel.rejectWorker(it) }
                    )
                    1 -> WorkersTab(
                        pendingWorkers = pendingWorkers,
                        approvedWorkers = approvedWorkers,
                        rejectedWorkers = rejectedWorkers,
                        blockedWorkers = blockedWorkers,
                        onApprove = { viewModel.approveWorker(it) },
                        onReject = { viewModel.rejectWorker(it) },
                        onBlock = { id, block -> viewModel.blockWorker(id, block) }
                    )
                    2 -> CustomersTab(customers = customers)
                    3 -> CategoriesTab(
                        categories = viewModel.categories.collectAsState().value,
                        onSave = { viewModel.saveCategory(it) },
                        onHide = { id, hide -> viewModel.hideCategory(id, hide) },
                        onDelete = { viewModel.deleteCategory(it) }
                    )
                    4 -> AnalyticsTab(analytics = analytics)
                }
            }
        }
    }
}

@Composable
fun DashboardTab(
    analytics: Map<String, Any>,
    pendingWorkers: List<Worker>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Total Users", value = analytics["totalUsers"]?.toString() ?: "0")
                StatCard(modifier = Modifier.weight(1f), title = "Pending Workers", value = analytics["pendingWorkers"]?.toString() ?: "0")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Pending Approvals", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (pendingWorkers.isEmpty()) {
            item {
                Text("No pending workers.", modifier = Modifier.padding(16.dp))
            }
        } else {
            items(pendingWorkers) { worker ->
                PendingWorkerCard(worker, onApprove, onReject)
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PendingWorkerCard(worker: Worker, onApprove: (String) -> Unit, onReject: (String) -> Unit) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(worker.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${worker.category} • ${worker.location}", style = MaterialTheme.typography.bodySmall)
            Text("Phone: ${worker.phone}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { onApprove(worker.id) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Approve")
                }
                OutlinedButton(onClick = { onReject(worker.id) }) {
                    Text("Reject")
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Call, contentDescription = "Call Worker")
                }
            }
        }
    }
}

@Composable
fun WorkersTab(
    pendingWorkers: List<Worker>,
    approvedWorkers: List<Worker>,
    rejectedWorkers: List<Worker>,
    blockedWorkers: List<Worker>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onBlock: (String, Boolean) -> Unit
) {
    var workerFilter by remember { mutableStateOf("Pending") }
    val filters = listOf("Pending", "Approved", "Rejected", "Blocked")
    var searchQuery by remember { mutableStateOf("") }
    
    val baseList = when (workerFilter) {
        "Pending" -> pendingWorkers
        "Approved" -> approvedWorkers
        "Rejected" -> rejectedWorkers
        "Blocked" -> blockedWorkers
        else -> emptyList()
    }
    
    val displayList = baseList.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Worker") },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
        )
        ScrollableTabRow(selectedTabIndex = filters.indexOf(workerFilter), edgePadding = 16.dp) {
            filters.forEach { filter ->
                Tab(
                    selected = workerFilter == filter,
                    onClick = { workerFilter = filter; searchQuery = "" },
                    text = { Text(filter) }
                )
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            items(displayList) { worker ->
                WorkerAdminCard(worker, onApprove, onReject, onBlock)
            }
        }
    }
}

@Composable
fun WorkerAdminCard(worker: Worker, onApprove: (String) -> Unit, onReject: (String) -> Unit, onBlock: (String, Boolean) -> Unit) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(worker.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(worker.workerStatus.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Text("${worker.category} • ${worker.phone}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (worker.workerStatus == "pending") {
                    Button(onClick = { onApprove(worker.id) }) { Text("Approve") }
                    OutlinedButton(onClick = { onReject(worker.id) }) { Text("Reject") }
                }
                if (worker.workerStatus == "approved" && !worker.isBlocked) {
                    Button(onClick = { onBlock(worker.id, true) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Block") }
                } else if (worker.isBlocked) {
                    Button(onClick = { onBlock(worker.id, false) }) { Text("Unblock") }
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Call, contentDescription = "Call Worker")
                }
            }
        }
    }
}

@Composable
fun CustomersTab(customers: List<User>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(customers) { user ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(user.email, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Joined: ${user.createdAt}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesTab(
    categories: List<com.example.domain.Category>,
    onSave: (com.example.domain.Category) -> Unit,
    onHide: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editCategory by remember { mutableStateOf<com.example.domain.Category?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            items(categories) { category ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(category.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(if (category.isHidden) "HIDDEN" else "VISIBLE", style = MaterialTheme.typography.labelSmall, color = if (category.isHidden) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        }
                        Text("Icon: ${category.iconName}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { editCategory = category; showDialog = true }) { Text("Edit") }
                            OutlinedButton(onClick = { onHide(category.id, !category.isHidden) }) { Text(if (category.isHidden) "Show" else "Hide") }
                            OutlinedButton(onClick = { onDelete(category.id) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
                        }
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { editCategory = null; showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Category")
        }
    }

    if (showDialog) {
        var name by remember { mutableStateOf(editCategory?.name ?: "") }
        var iconName by remember { mutableStateOf(editCategory?.iconName ?: "") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editCategory == null) "New Category" else "Edit Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = iconName,
                        onValueChange = { iconName = it },
                        label = { Text("Icon Name (e.g. Electrician)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val cat = editCategory?.copy(name = name, iconName = iconName) ?: com.example.domain.Category(
                        id = java.util.UUID.randomUUID().toString(),
                        name = name,
                        iconName = iconName
                    )
                    onSave(cat)
                    showDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AnalyticsTab(analytics: Map<String, Any>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text("System Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            StatCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), title = "Total Users", value = analytics["totalUsers"]?.toString() ?: "0")
            StatCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), title = "Total Workers", value = analytics["totalWorkers"]?.toString() ?: "0")
            StatCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), title = "Approved Workers", value = analytics["approvedWorkers"]?.toString() ?: "0")
            StatCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), title = "Pending Workers", value = analytics["pendingWorkers"]?.toString() ?: "0")
            StatCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), title = "Total Categories", value = analytics["totalCategories"]?.toString() ?: "0")
            
            Text("Engagement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
            StatCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), title = "Total Favorites", value = analytics["totalFavorites"]?.toString() ?: "0")
            StatCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), title = "Total Reviews", value = analytics["totalReviews"]?.toString() ?: "0")
            StatCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), title = "Most Searched Categories", value = "N/A")
            StatCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), title = "Most Active Areas", value = "N/A")
        }
    }
}
