package com.inventtrack.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.inventtrack.ui.components.ScreenContent
import com.inventtrack.ui.screens.*
import kotlinx.coroutines.delay

@Composable
fun InventTrackApp() {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
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
        NavHost(navController, startDestination = Routes.SPLASH, modifier = Modifier.padding(padding)) {
            composable(Routes.SPLASH) {
                ScreenContent("InventTrack") { Text("Tu inventario, bajo control"); CircularProgressIndicator() }
                LaunchedEffect(Unit) {
                    delay(900)
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } }
                }
            }
            composable(Routes.LOGIN) {
                IntegrationScreen("Iniciar sesión", listOf("Entrar en modo demo" to {
                    navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }, "Crear cuenta" to { go(Routes.REGISTER) }))
            }
            composable(Routes.REGISTER) {
                IntegrationScreen("Crear cuenta", listOf("Volver a iniciar sesión" to { navController.popBackStack() }))
            }
            composable(Routes.HOME) {
                IntegrationScreen("Inicio", listOf("Ver productos" to { go(Routes.PRODUCTS) },
                    "Registrar movimiento" to { go(Routes.MOVEMENT_FORM) }, "Análisis y estadísticas" to { go(Routes.ANALYTICS) }))
            }
            composable(Routes.PRODUCTS) {
                IntegrationScreen("Productos", listOf("Nuevo producto" to { go(Routes.PRODUCT_FORM) }, "Categorías" to { go(Routes.CATEGORIES) }))
            }
            composable(Routes.PRODUCT_FORM) { IntegrationScreen("Formulario de producto") }
            composable(Routes.CATEGORIES) { IntegrationScreen("Categorías") }
            composable(Routes.HISTORY) { IntegrationScreen("Historial de movimientos") }
            composable(Routes.CHATBOT) { IntegrationScreen("Chatbot") }
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
