package com.inventtrack.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.model.Producto
import com.inventtrack.data.repository.ProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductStockUiState(
    val products: List<Producto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/** Adaptador mínimo de la lista existente para mostrar el stock compartido. */
class ProductStockViewModel(private val productRepository: ProductRepository) : ViewModel() {
    val uiState = productRepository.observeProducts()
        .map { ProductStockUiState(products = it, isLoading = false) }
        .catch { emit(ProductStockUiState(isLoading = false, error = "No se pudieron cargar los productos.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductStockUiState())

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun delete(id: Int) {
        viewModelScope.launch {
            try {
                productRepository.delete(id)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _events.emit("No se pudo eliminar el producto.")
            }
        }
    }
}
