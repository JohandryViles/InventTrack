package com.inventtrack.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.inventtrack.ui.components.ScreenContent

/** Punto de integración para las pantallas asignadas a los demás integrantes. */
@Composable
fun IntegrationScreen(title: String, actions: List<Pair<String, () -> Unit>> = emptyList()) {
    ScreenContent(title) {
        Text("Base de navegación · pantalla pendiente de integración", style = MaterialTheme.typography.bodyMedium)
        actions.forEach { (label, action) ->
            FilledTonalButton(onClick = action, modifier = Modifier.fillMaxWidth()) { Text(label) }
        }
    }
}
