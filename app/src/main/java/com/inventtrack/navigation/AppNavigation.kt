package com.inventtrack.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.inventtrack.ui.screens.*

@Composable
fun InventTrackApp() {
    val navController = rememberNavController()
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
                    onLoginClick = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onRegisterClick = { go(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onBackClick = { navController.popBackStack() },
                    onRegisterClick = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    }
                )
            }
            composable(Routes.HOME) {
                DashboardScreen(
                    onRegisterMovementClick = { go(Routes.MOVEMENT_FORM) },
                    onAddProductClick = { go(Routes.PRODUCT_FORM) },
                    onOpenChatbotClick = { go(Routes.CHATBOT) }
                )
            }
            composable(Routes.PRODUCTS) {
                ProductsScreen(
                    onNewProduct = { go(Routes.PRODUCT_FORM) },
                    onCategories = { go(Routes.CATEGORIES) }
                )
            }
            composable(Routes.PRODUCT_FORM) { IntegrationScreen("Formulario de producto") }
            composable(Routes.CATEGORIES) { IntegrationScreen("Categorías") }
            composable(Routes.HISTORY) {
                MovementHistoryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Routes.CHATBOT) { InvenBotScreen() }
            composable(Routes.MOVEMENT_FORM) { MovementScreen() }
            composable(Routes.ANALYTICS) { AnalyticsScreen() }
            composable(Routes.SETTINGS) {
                IntegrationScreen("Más", listOf("Categorías" to { go(Routes.CATEGORIES) },
                    "Historial" to { go(Routes.HISTORY) }, "Análisis y estadísticas" to { go(Routes.ANALYTICS) },
                    "Cerrar sesión demo" to {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }))
            }
        }
    }
}
