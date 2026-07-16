package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(val route: String, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector, val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector, val label: String) {
    object Home : BottomNavItem("home_tab", Icons.Filled.Home, Icons.Outlined.Home, "Home")
    object Search : BottomNavItem("search_tab", Icons.Filled.Search, Icons.Outlined.Search, "Search")
    object Bookings : BottomNavItem("bookings_tab", Icons.Filled.List, Icons.Outlined.List, "Bookings")
    object Notifications : BottomNavItem("notifications_tab", Icons.Filled.Notifications, Icons.Outlined.Notifications, "Alerts")
    object Profile : BottomNavItem("profile_tab", Icons.Filled.Person, Icons.Outlined.Person, "Profile")
}

@Composable
fun MainAppScreen(
    onCategoryClick: (String) -> Unit,
    onWorkerClick: (String) -> Unit,
    onNavigateToWorkerRegistration: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Bookings,
        BottomNavItem.Notifications,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = items.any { it.route == currentRoute },
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .background(Color.Transparent)
                            .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = Color(0x33000000))
                            .clip(RoundedCornerShape(32.dp)),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        tonalElevation = 0.dp
                    ) {
                        items.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = null, // Premium minimal look: hide labels or use standard ones
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(bottom = 0.dp) // We handle bottom padding inside the screens since it's floating
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onCategoryClick = onCategoryClick,
                    onNavigateToSearch = { 
                        navController.navigate(BottomNavItem.Search.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToWorkerRegistration = onNavigateToWorkerRegistration,
                    onWorkerClick = onWorkerClick
                )
            }
            composable(BottomNavItem.Search.route) {
                SearchScreen(
                    onWorkerSelected = { worker ->
                        onWorkerClick(worker.uid)
                    }
                )
            }
            composable(BottomNavItem.Bookings.route) {
                MyBookingsScreen()
            }
            composable(BottomNavItem.Notifications.route) {
                NotificationsScreen()
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToHelpCenter = { navController.navigate("help_center") },
                    onNavigateToWorkerRegistration = { navController.navigate("worker_registration") },
                    onNavigateToEditProfile = { navController.navigate("edit_profile") },
                    onLogout = onLogout
                )
            }
            composable("edit_profile") {
                EditProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToAbout = { navController.navigate("about") },
                    onNavigateToPrivacy = { navController.navigate("privacy") },
                    onNavigateToTerms = { navController.navigate("terms") }
                )
            }
            composable("worker_registration") {
                com.example.ui.WorkerRegistrationScreen(
                    onSubmit = { navController.navigate("worker_registration_success") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("about") {
                InfoScreen(title = "About Lokivo", content = "Lokivo is India's next-generation Local Services Marketplace, connecting customers with trusted nearby workers. Discover, compare, contact, and review professionals instantly.", onBack = { navController.popBackStack() })
            }
            composable("privacy") {
                InfoScreen(title = "Privacy Policy", content = "Your privacy is important to us. This Privacy Policy outlines how we collect, use, and protect your information when you use our platform.", onBack = { navController.popBackStack() })
            }
            composable("terms") {
                InfoScreen(title = "Terms & Conditions", content = "By using Lokivo, you agree to abide by these terms. We provide a platform for connecting customers with service professionals. We are not liable for the quality of services provided by third-party workers.", onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text(title, style = MaterialTheme.typography.displayLarge)
    }
}
