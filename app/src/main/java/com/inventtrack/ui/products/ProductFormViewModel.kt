package com.inventtrack.ui.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.Producto
import com.inventtrack.data.ProductRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductFormUiState(
    val nombre: String = "",
    val categoria: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val stockActual: String = "",
    val stockMinimo: String = "",
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val priceError: String? = null,
    val stockError: String? = null
)

class ProductFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val productId: String? = savedStateHandle["id"]

    private val _uiState = MutableStateFlow(ProductFormUiState())
    val uiState: StateFlow<ProductFormUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    init {
        productId?.toIntOrNull()?.let { id ->
            viewModelScope.launch {
                productRepository.getProductById(id)?.let { producto ->
                    _uiState.update {
                        it.copy(
                            nombre = producto.nombre,
                            categoria = producto.categoria,
                            descripcion = producto.descripcion,
                            precio = producto.precio.toString(),
                            stockActual = producto.stockActual.toString(),
                            stockMinimo = producto.stockMinimo.toString()
                        )
                    }
                }
            }
        }
    }

    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre, nameError = null) }
    }

    fun onCategoriaChange(categoria: String) {
        _uiState.update { it.copy(categoria = categoria) }
    }

    fun onDescripcionChange(descripcion: String) {
        _uiState.update { it.copy(descripcion = descripcion) }
    }

    fun onPrecioChange(precio: String) {
        _uiState.update { it.copy(precio = precio, priceError = null) }
    }

    fun onStockActualChange(stock: String) {
        _uiState.update { it.copy(stockActual = stock, stockError = null) }
    }

    fun onStockMinimoChange(stock: String) {
        _uiState.update { it.copy(stockMinimo = stock) }
    }

    fun saveProduct() {
        val currentState = _uiState.value
        val nameValid = currentState.nombre.isNotBlank()
        val priceValid = currentState.precio.toDoubleOrNull() != null
        val stockValid = currentState.stockActual.toIntOrNull() != null

        if (!nameValid || !priceValid || !stockValid) {
            _uiState.update {
                it.copy(
                    nameError = if (nameValid) null else "El nombre no puede estar vacío",
                    priceError = if (priceValid) null else "Precio inválido",
                    stockError = if (stockValid) null else "Stock inválido"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val product = Producto(
                id = productId?.toIntOrNull() ?: 0,
                nombre = currentState.nombre,
                categoria = currentState.categoria,
                descripcion = currentState.descripcion,
                precio = currentState.precio.toDouble(),
                stockActual = currentState.stockActual.toInt(),
                stockMinimo = currentState.stockMinimo.toIntOrNull() ?: 0
            )

            if (product.id == 0) {
                productRepository.addProduct(product)
            } else {
                productRepository.updateProduct(product)
            }

            _uiState.update { it.copy(isSaving = false) }
            _navigateBack.emit(Unit)
        }
    }
}
