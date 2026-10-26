package com.inventtrack.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.repository.AuthRepository
import com.inventtrack.data.repository.ProductRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Tiempo mínimo en pantalla para que el logo se alcance a ver aunque la carga sea rápida.
internal const val SPLASH_MIN_DURATION_MS = 2_000L

enum class SplashDestination { LOGIN, HOME }

data class SplashUiState(
    val progress: Float = 0f,
    val message: String = "Iniciando...",
    /** Se llena al terminar la carga; la pantalla navega cuando deja de ser null. */
    val destination: SplashDestination? = null
)

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val minimumDuration = async { delay(SPLASH_MIN_DURATION_MS) }

            _uiState.update { it.copy(progress = 0.3f, message = "Cargando inventario...") }
            val productos = productRepository.observeProducts().first()

            _uiState.update { it.copy(progress = 0.7f, message = "Inventario listo: ${productos.size} productos") }
            val haySesion = authRepository.currentUser.value != null

            minimumDuration.await()
            _uiState.update {
                it.copy(
                    progress = 1f,
                    message = "Listo",
                    destination = if (haySesion) SplashDestination.HOME else SplashDestination.LOGIN
                )
            }
        }
    }
}
