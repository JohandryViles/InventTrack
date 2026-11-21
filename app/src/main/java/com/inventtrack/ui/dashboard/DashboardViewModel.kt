package com.inventtrack.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.model.Movimiento
import com.inventtrack.data.model.Producto
import com.inventtrack.data.repository.AuthRepository
import com.inventtrack.data.repository.MovementRepository
import com.inventtrack.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate

data class DashboardUiState(
    val nombreUsuario: String = "",
    val totalProductos: Int = 0,
    val stockBajo: Int = 0,
    val entradasHoy: Int = 0,
    val salidasHoy: Int = 0,
    val movimientosRecientes: List<Movimiento> = emptyList(),
    val alertas: List<String> = emptyList(),
    val productosCriticos: List<Producto> = emptyList(),
    val isLoading: Boolean = false
)

class DashboardViewModel(
    authRepository: AuthRepository,
    productRepository: ProductRepository,
    movementRepository: MovementRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        authRepository.currentUser,
        productRepository.observeProducts(),
        movementRepository.observeMovements()
    ) { user, products, movements ->
        val today = LocalDate.now(clock)
        val lowStockProducts = products.filter { it.tieneStockBajo }
        val todayMovements = movements.filter { it.fecha.toLocalDate() == today }
        val entradasHoy = todayMovements.filter { it.esEntrada }.sumOf { it.cantidad }
        val salidasHoy = todayMovements.filter { !it.esEntrada }.sumOf { it.cantidad }
        val alertas = lowStockProducts.map { "${it.nombre} alcanzó el stock mínimo" }

        DashboardUiState(
            nombreUsuario = user?.nombre.orEmpty(),
            totalProductos = products.size,
            stockBajo = lowStockProducts.size,
            entradasHoy = entradasHoy,
            salidasHoy = salidasHoy,
            movimientosRecientes = movements.take(5),
            alertas = alertas,
            productosCriticos = lowStockProducts,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(isLoading = true)
    )
}
