package com.inventtrack.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.Categoria
import com.inventtrack.data.CategoryRepository
import com.inventtrack.data.Producto
import com.inventtrack.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductListUiState(
    val isLoading: Boolean = false,
    val productos: List<Producto> = emptyList(),
    val categorias: List<String> = emptyList(),
    val query: String = "",
    val categoriaSeleccionada: String = "Todos",
    val productoAEliminar: Producto? = null
)

class ProductListViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _categoriaSeleccionada = MutableStateFlow("Todos")
    private val _productoAEliminar = MutableStateFlow<Producto?>(null)
    private val _isLoading = MutableStateFlow(false) // Esto puede cambiar según la carga real

    private data class FilterState(
        val query: String,
        val categoriaSeleccionada: String,
        val productoAEliminar: Producto?,
        val isLoading: Boolean
    )

    private val _filterState = combine(
        _query, _categoriaSeleccionada, _productoAEliminar, _isLoading
    ) { q, cat, prod, loading -> FilterState(q, cat, prod, loading) }

    val uiState: StateFlow<ProductListUiState> = combine(
        productRepository.observeProducts(),
        categoryRepository.observeCategories(),
        _filterState
    ) { productos, categorias, filter ->
        val filteredProducts = productos.filter {
            (filter.categoriaSeleccionada == "Todos" || it.categoria == filter.categoriaSeleccionada) &&
                    it.nombre.contains(filter.query, ignoreCase = true)
        }
        val categoriesList = listOf("Todos") + categorias.map { it.nombre }

        ProductListUiState(
            isLoading = filter.isLoading,
            productos = filteredProducts,
            categorias = categoriesList,
            query = filter.query,
            categoriaSeleccionada = filter.categoriaSeleccionada,
            productoAEliminar = filter.productoAEliminar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductListUiState(isLoading = true)
    )

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onCategoryChange(categoria: String) {
        _categoriaSeleccionada.value = categoria
    }

    fun onDeleteRequest(producto: Producto?) {
        _productoAEliminar.value = producto
    }

    fun confirmDelete() {
        val producto = _productoAEliminar.value ?: return
        viewModelScope.launch {
            _isLoading.update { true }
            productRepository.deleteProduct(producto.id)
            _productoAEliminar.value = null
            _isLoading.update { false }
        }
    }
}
