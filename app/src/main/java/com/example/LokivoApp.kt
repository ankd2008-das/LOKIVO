package com.example

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.CategoryWorkersScreen
import com.example.ui.GetStartedScreen
import com.example.ui.LoginScreen
import com.example.ui.RegisterScreen
import com.example.ui.ForgotPasswordScreen
import com.example.ui.SuccessScreen
import com.example.ui.SplashScreen
import com.example.ui.MainAppScreen
import com.example.ui.OtpVerificationScreen
import com.example.ui.ChooseRoleScreen
import com.example.ui.LocationPermissionScreen
import com.example.ui.OnboardingScreen
import com.example.ui.WorkerProfileScreen

@Composable
fun LokivoApp() {
    val navController = rememberNavController()
    val authVm: com.example.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val initializer = com.example.data.repository.AppInitializer()
        initializer.initializeDataIfEmpty()
    }

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
    ) {
        composable("splash") {
            SplashScreen(
                viewModel = authVm,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToWorker = {
                    navController.navigate("worker_main") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate("admin_dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    navController.navigate("get_started") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("get_started") {
            GetStartedScreen(
                onContinue = { navController.navigate("login") },
                onSkip = {
                    navController.navigate("main") {
                        popUpTo("get_started") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = authVm,
                onLoginSuccess = { role ->
                    if (role == "admin" || role == "super_admin") {
                        navController.navigate("admin_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else if (role == "worker" || role == "professional") {
                        navController.navigate("worker_main") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgot = { navController.navigate("forgot_password") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("otp_verification")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("otp_verification") {
            OtpVerificationScreen(
                onVerifySuccess = { navController.navigate("choose_role") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("choose_role") {
            ChooseRoleScreen(
                onNavigateToCustomer = {
                    
                    authVm.updateRole("customer", "customer")
                    navController.navigate("location_permission")
                },
                onNavigateToWorker = {
                    
                    authVm.updateRole("worker", "professional")
                    navController.navigate("worker_main") { popUpTo("choose_role") { inclusive = true } }
                },
                onNavigateToRegisterWorker = {
                    navController.navigate("worker_registration")
                }
            )
        }
        composable("worker_registration") {
            com.example.ui.WorkerRegistrationScreen(
                onSubmit = { navController.navigate("worker_registration_success") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("worker_registration_success") {
            com.example.ui.WorkerRegistrationSuccessScreen(
                onTrackApplication = { navController.navigate("pending_approval") { popUpTo(0) } },
                onBackToHome = { navController.navigate("main") { popUpTo(0) } }
            )
        }
        composable("pending_approval") {
            com.example.ui.WorkerPendingApprovalScreen(
                onCheckStatus = { navController.navigate("worker_main") { popUpTo(0) } }
            )
        }
        composable("worker_main") {
            com.example.ui.WorkerMainScreen(
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToSupport = { navController.navigate("help_center") },
                onNavigateToEditProfile = { navController.navigate("worker_edit_profile") },
                onNavigateToReviews = { navController.navigate("worker_reviews") },
                onNavigateToCustomerMode = {
                    
                    authVm.updateRole("customer")
                    navController.navigate("main") { popUpTo(0) }
                },
                onLogout = {
                    
                    authVm.logout {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("worker_edit_profile") {
            com.example.ui.WorkerEditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("worker_reviews") {
            com.example.ui.WorkerReviewsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("location_permission") {
            LocationPermissionScreen(
                onAllow = { navController.navigate("success") },
                onSkip = { navController.navigate("success") }
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onSendReset = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable("success") {
            SuccessScreen(
                onContinue = {
                    navController.navigate("main") {
                        popUpTo("success") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainAppScreen(
                onCategoryClick = { categoryName ->
                    navController.navigate("category/$categoryName")
                },
                onWorkerClick = { workerId ->
                    navController.navigate("worker/$workerId")
                },
                onNavigateToWorkerRegistration = {
                    navController.navigate("worker_registration")
                },
                onLogout = {
                    
                    authVm.logout {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("category/{categoryName}") { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryWorkersScreen(
                categoryName = categoryName,
                onBack = { navController.popBackStack() },
                onWorkerClick = { workerId ->
                    navController.navigate("worker/$workerId")
                }
            )
        }
        composable("worker/{workerId}") { backStackEntry ->
            val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
            WorkerProfileScreen(
                workerId = workerId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("admin_dashboard") {
            com.example.ui.components.AdminRouteGuard(
                onUnauthorized = { navController.popBackStack() }
            ) {
                com.example.ui.AdminDashboardScreen(
                    onLogout = {
                        
                        authVm.logout {
                            navController.navigate("login") { popUpTo(0) }
                        }
                    }
                )
            }
        }
        composable("settings") {
            com.example.ui.SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate("about") },
                onNavigateToPrivacy = { navController.navigate("privacy") },
                onNavigateToTerms = { navController.navigate("terms") }
            )
        }
        composable("help_center") {
            com.example.ui.HelpCenterScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("about") {
            com.example.ui.InfoScreen(
                title = "About Lokivo",
                content = "Lokivo is India's next-generation Local Services Marketplace, connecting customers with trusted nearby workers. Discover, compare, contact, and review professionals instantly.",
                onBack = { navController.popBackStack() }
            )
        }
        composable("privacy") {
            com.example.ui.InfoScreen(
                title = "Privacy Policy",
                content = "Your privacy is important to us. This Privacy Policy outlines how we collect, use, and protect your information when you use our platform.",
                onBack = { navController.popBackStack() }
            )
        }
        composable("terms") {
            com.example.ui.InfoScreen(
                title = "Terms & Conditions",
                content = "By using Lokivo, you agree to abide by these terms. We provide a platform for connecting customers with service professionals. We are not liable for the quality of services provided by third-party workers.",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
