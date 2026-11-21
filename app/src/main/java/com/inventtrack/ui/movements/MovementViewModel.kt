package com.inventtrack.ui.movements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.model.Producto
import com.inventtrack.data.model.TipoMovimiento
import com.inventtrack.data.repository.MovementRepository
import com.inventtrack.data.repository.ProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MovementUiState(
    val productos: List<Producto> = emptyList(),
    val tipo: TipoMovimiento = TipoMovimiento.ENTRADA,
    val productoSeleccionado: Int? = null,
    val cantidad: String = "",
    val fecha: LocalDate = LocalDate.now(),
    val nota: String = "",
    val error: String? = null,
    val confirmacion: String? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = true
) {
    val producto: Producto? get() = productos.find { it.id == productoSeleccionado }
}

class MovementViewModel(
    private val productRepository: ProductRepository,
    private val movementRepository: MovementRepository
) : ViewModel() {
    private val form = MutableStateFlow(MovementUiState())

    val uiState: StateFlow<MovementUiState> = combine(
        productRepository.observeProducts(), form
    ) { products, state -> state.copy(productos = products, isLoading = false) }
        .catch { emit(form.value.copy(isLoading = false, error = "No se pudieron cargar los productos.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), form.value)

    private fun edit(transform: (MovementUiState) -> MovementUiState) {
        form.update { if (it.isSaving) it else transform(it).copy(error = null, confirmacion = null) }
    }

    fun onTipoChange(tipo: TipoMovimiento) = edit { it.copy(tipo = tipo) }
    fun onProductoChange(id: Int) = edit { it.copy(productoSeleccionado = id) }
    fun onCantidadChange(cantidad: String) = edit { it.copy(cantidad = cantidad) }
    fun onFechaChange(fecha: LocalDate) = edit { it.copy(fecha = fecha) }
    fun onNotaChange(nota: String) = edit { it.copy(nota = nota) }
    fun dismissConfirmation() { form.update { it.copy(confirmacion = null) } }

    fun registrar() {
        val state = form.value
        if (state.isSaving) return
        val amount = state.cantidad.trim().toIntOrNull()
        val error = when {
            state.productoSeleccionado == null -> "Selecciona un producto."
            state.cantidad.isBlank() -> "Ingresa una cantidad."
            amount == null -> "Ingresa una cantidad entera dentro del límite permitido."
            amount <= 0 -> "La cantidad debe ser mayor que cero."
            else -> null
        }
        if (error != null) {
            form.update { it.copy(error = error) }
            return
        }
        val productId = requireNotNull(state.productoSeleccionado)
        val quantity = requireNotNull(amount)
        // Se bloquea antes de lanzar la corrutina para evitar registros por doble toque.
        form.update { it.copy(isSaving = true, error = null, confirmacion = null) }
        viewModelScope.launch {
            try {
                val product = productRepository.getProduct(productId)
                val stockError = when {
                    product == null -> "El producto ya no existe. Selecciona otro."
                    state.tipo == TipoMovimiento.SALIDA && quantity > product.stockActual ->
                        "La salida supera las existencias disponibles (${product.stockActual})."
                    state.tipo == TipoMovimiento.ENTRADA && quantity > Int.MAX_VALUE - product.stockActual ->
                        "La cantidad supera el límite permitido."
                    else -> null
                }
                if (stockError != null) {
                    form.update { it.copy(error = stockError) }
                    return@launch
                }
                movementRepository.register(
                    productId, state.tipo, quantity, state.fecha.atTime(java.time.LocalTime.now()), state.nota
                ).onSuccess { movement ->
                    val type = if (movement.esEntrada) "Entrada" else "Salida"
                    form.update {
                        it.copy(cantidad = "", nota = "", confirmacion =
                            "$type de ${movement.cantidad} unidades de ${movement.productoNombre} registrada.")
                    }
                }.onFailure { failure ->
                    if (failure is CancellationException) throw failure
                    form.update { it.copy(error = failure.message ?: "No se pudo registrar el movimiento.") }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                form.update { it.copy(error = failure.message ?: "No se pudo registrar el movimiento.") }
            } finally {
                form.update { it.copy(isSaving = false) }
            }
        }
    }
}
