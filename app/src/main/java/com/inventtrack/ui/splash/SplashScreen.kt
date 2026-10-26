package com.inventtrack.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventtrack.di.AppViewModelProvider
import com.inventtrack.ui.theme.PrimaryDarkBlue
import com.inventtrack.ui.theme.SecondaryTeal
import com.inventtrack.ui.theme.SurfaceLight

private const val PROGRESS_ANIMATION_MS = 600

@Composable
fun SplashRoute(
    onFinished: (SplashDestination) -> Unit,
    viewModel: SplashViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnFinished by rememberUpdatedState(onFinished)
    // La animación es estado de UI: suaviza los saltos de progreso que publica el ViewModel.
    val progress = remember { Animatable(0f) }

    LaunchedEffect(uiState.progress, uiState.destination) {
        progress.animateTo(
            targetValue = uiState.progress,
            animationSpec = tween(durationMillis = PROGRESS_ANIMATION_MS, easing = LinearEasing)
        )
        // Navega cuando la barra llegó al final, no antes.
        uiState.destination?.let(currentOnFinished)
    }

    SplashScreen(progress = { progress.value }, message = uiState.message)
}

@Composable
fun SplashScreen(progress: () -> Float, message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryDarkBlue),
        contentAlignment = Alignment.Center
    ) {
        // Logo y nombre de la aplicación
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(SecondaryTeal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Logo InventTrack",
                    modifier = Modifier.size(56.dp),
                    tint = SecondaryTeal
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "InventTrack",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = SurfaceLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tu inventario, bajo control",
                style = MaterialTheme.typography.bodyMedium,
                color = SurfaceLight.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // Barra de carga con el avance real de la carga inicial
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape),
                color = SecondaryTeal,
                trackColor = SurfaceLight.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = SurfaceLight.copy(alpha = 0.6f)
            )
        }
    }
}
