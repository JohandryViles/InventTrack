package com.inventtrack.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.inventtrack.di.AppViewModelProvider
import com.inventtrack.ui.screens.*
import com.inventtrack.ui.session.SessionViewModel
import com.inventtrack.ui.settings.SettingsRoute

@Composable
fun InventTrackApp(
    sessionViewModel: SessionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val navController = rememberNavController()
    val currentUser by sessionViewModel.currentUser.collectAsStateWithLifecycle()
    val userName = currentUser?.nombre.orEmpty()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val isSplash = route == Routes.SPLASH
    val authenticated = route != null && route !in listOf(Routes.SPLASH, Routes.LOGIN, Routes.REGISTER)
    val tabs = listOf(
        Triple(Routes.HOME, "Inicio", Icons.Default.Home),
        Triple(Routes.PRODUCTS, "Productos", Icons.Default.Inventory2),
        Triple(Routes.MOVEMENT_FORM, "Movim.", Icons.Default.SwapVert),
        Triple(Routes.CHATBOT, "Chatbot", Icons.AutoMirrored.Filled.Chat),
        Triple(Routes.SETTINGS, "Más", Icons.Default.MoreHoriz)
    )
    fun go(destination: String) { navController.navigate(destination) { launchSingleTop = true } }
    Scaffold(bottomBar = {
        if (authenticated) NavigationBar {
            tabs.forEach { (destination, label, icon) ->
                val selected = route == destination ||
                    (destination == Routes.PRODUCTS && route == Routes.PRODUCT_FORM) ||
                    (destination == Routes.SETTINGS && route in listOf(Routes.CATEGORIES, Routes.HISTORY, Routes.ANALYTICS))
                NavigationBarItem(selected = selected, onClick = {
                    navController.navigate(destination) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }, icon = { Icon(icon, contentDescription = label) }, label = { Text(label, maxLines = 1) })
            }
        }
    }) { padding ->
        NavHost(
            navController,
            startDestination = Routes.SPLASH,
            modifier = if (isSplash) Modifier else Modifier.padding(padding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onTimeout = {
                        navController.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } }
                    }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginClick = { name, email ->
                        sessionViewModel.startSession(name, email)
                        navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onRegisterClick = { go(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onBackClick = { navController.popBackStack() },
                    onRegisterClick = { name, email ->
                        sessionViewModel.startSession(name, email)
                        navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    }
                )
            }
            composable(Routes.HOME) {
                DashboardScreen(
                    userName = userName,
                    onRegisterMovementClick = { go(Routes.MOVEMENT_FORM) },
                    onAddProductClick = { go(Routes.PRODUCT_FORM) },
                    onOpenChatbotClick = { go(Routes.CHATBOT) }
                )
            }
            composable(Routes.PRODUCTS) {
                ProductListScreen(
                    onNavigateToAdd = { go(Routes.PRODUCT_FORM) },
                    onNavigateToEdit = { _ -> go(Routes.PRODUCT_FORM) }
                )
            }
            composable(Routes.PRODUCT_FORM) { 
                ProductFormScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSave = { _ -> navController.popBackStack() }
                ) 
            }
            composable(Routes.CATEGORIES) { CategoryScreen() }
            composable(Routes.HISTORY) {
                MovementHistoryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Routes.CHATBOT) { InvenBotScreen() }
            composable(Routes.MOVEMENT_FORM) { MovementScreen() }
            composable(Routes.ANALYTICS) { AnalyticsScreen() }
            composable(Routes.SETTINGS) {
                SettingsRoute(
                    onCategoriesClick = { go(Routes.CATEGORIES) },
                    onHistoryClick = { go(Routes.HISTORY) },
                    onAnalyticsClick = { go(Routes.ANALYTICS) },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
