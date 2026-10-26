package com.inventtrack.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ProductRepository(private val ioDispatcher: CoroutineDispatcher) {
    private val _productos = MutableStateFlow(
        listOf(
            Producto(1, "Azitromicina 500 mg", "Antibióticos", "Caja con 3 tabletas", 150.0, 10, 5),
            Producto(2, "Suero fisiológico", "Insumos", "Botella 500ml", 45.0, 3, 10),
            Producto(3, "Jarabe pediátrico", "Jarabes", "Frasco 120ml", 80.0, 20, 5),
            Producto(4, "Paracetamol 500 mg", "Analgésicos", "Caja con 20 tabletas", 25.0, 50, 10)
        )
    )
    private var nextId = 5

    fun observeProducts(): Flow<List<Producto>> {
        return _productos.asStateFlow()
    }

    suspend fun getProductById(id: Int): Producto? = withContext(ioDispatcher) {
        delay(500)
        _productos.value.find { it.id == id }
    }

    suspend fun addProduct(producto: Producto) = withContext(ioDispatcher) {
        delay(500)
        val newProduct = producto.copy(id = nextId++)
        _productos.value = _productos.value + newProduct
    }

    suspend fun updateProduct(producto: Producto) = withContext(ioDispatcher) {
        delay(500)
        _productos.value = _productos.value.map {
            if (it.id == producto.id) producto else it
        }
    }

    suspend fun deleteProduct(id: Int) = withContext(ioDispatcher) {
        delay(500)
        _productos.value = _productos.value.filter { it.id != id }
    }
}
