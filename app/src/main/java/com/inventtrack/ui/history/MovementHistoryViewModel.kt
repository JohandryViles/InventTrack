package com.inventtrack.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.model.Movimiento
import com.inventtrack.data.model.TipoMovimiento
import com.inventtrack.data.repository.MovementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MovementHistoryUiState(
    val groupedMovements: Map<String, List<Movimiento>> = emptyMap(),
    val productOptions: List<String> = listOf("Todos"),
    val selectedDate: String = "Todas",
    val selectedType: String = "Todos",
    val selectedProduct: String = "Todos",
    val selectedMovement: Movimiento? = null,
    val isLoading: Boolean = false
) {
    val isAnyFilterActive: Boolean
        get() = selectedDate != "Todas" || selectedType != "Todos" || selectedProduct != "Todos"
}

class MovementHistoryViewModel(
    private val movementRepository: MovementRepository
) : ViewModel() {

    private val filtroFecha = MutableStateFlow("Todas")
    private val filtroTipo = MutableStateFlow("Todos")
    private val filtroProducto = MutableStateFlow("Todos")
    private val selectedMovement = MutableStateFlow<Movimiento?>(null)

    val uiState: StateFlow<MovementHistoryUiState> = combine(
        movementRepository.observeMovements(),
        filtroFecha,
        filtroTipo,
        filtroProducto,
        selectedMovement
    ) { movements, fechaFiltro, tipoFiltro, productoFiltro, currentSelectedMovement ->
        val productOptions = listOf("Todos") + movements.map { it.productoNombre }.distinct().sorted()

        val hoy = LocalDate.now()
        val ayer = hoy.minusDays(1)

        val filtered = movements.filter { mov ->
            val dateMatch = when (fechaFiltro) {
                "Hoy" -> mov.fecha.toLocalDate() == hoy
                "Ayer" -> mov.fecha.toLocalDate() == ayer
                else -> true
            }
            val typeMatch = when (tipoFiltro) {
                "ENTRADA" -> mov.tipo == TipoMovimiento.ENTRADA
                "SALIDA" -> mov.tipo == TipoMovimiento.SALIDA
                else -> true
            }
            val productMatch = when (productoFiltro) {
                "Todos" -> true
                else -> mov.productoNombre == productoFiltro
            }
            dateMatch && typeMatch && productMatch
        }

        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es"))
        val grouped = filtered.groupBy { mov ->
            val date = mov.fecha.toLocalDate()
            when (date) {
                hoy -> "Hoy — ${date.format(formatter)}"
                ayer -> "Ayer — ${date.format(formatter)}"
                else -> date.format(formatter)
            }
        }

        MovementHistoryUiState(
            groupedMovements = grouped,
            productOptions = productOptions,
            selectedDate = fechaFiltro,
            selectedType = tipoFiltro,
            selectedProduct = productoFiltro,
            selectedMovement = currentSelectedMovement,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MovementHistoryUiState(isLoading = true)
    )

    fun onDateFilterChange(fecha: String) {
        filtroFecha.update { fecha }
    }

    fun onTypeFilterChange(tipo: String) {
        filtroTipo.update { tipo }
    }

    fun onProductFilterChange(producto: String) {
        filtroProducto.update { producto }
    }

    fun clearFilters() {
        filtroFecha.update { "Todas" }
        filtroTipo.update { "Todos" }
        filtroProducto.update { "Todos" }
    }

    fun selectMovement(movimiento: Movimiento) {
        selectedMovement.update { movimiento }
    }

    fun dismissDetail() {
        selectedMovement.update { null }
    }
}
