package com.inventtrack.data.repository.fake

import com.inventtrack.data.model.Producto
import com.inventtrack.data.repository.ProductRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

class FakeProductRepository(
    seed: List<Producto>,
    private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {

    // Fuente única de verdad del inventario: todas las pantallas leen de aquí.
    private val products = MutableStateFlow(seed)
    private val nextId = AtomicInteger((seed.maxOfOrNull { it.id } ?: 0) + 1)

    override fun observeProducts(): Flow<List<Producto>> = products
        .onStart { delay(READ_DELAY_MS) }
        .flowOn(ioDispatcher)

    override fun observeProduct(id: Int): Flow<Producto?> = products
        .map { list -> list.find { it.id == id } }
        .distinctUntilChanged()
        .onStart { delay(READ_DELAY_MS) }
        .flowOn(ioDispatcher)

    override suspend fun getProduct(id: Int): Producto? = withContext(ioDispatcher) {
        delay(READ_DELAY_MS)
        products.value.find { it.id == id }
    }

    override suspend fun upsert(producto: Producto): Producto = withContext(ioDispatcher) {
        delay(WRITE_DELAY_MS)
        val saved = if (producto.id == 0) producto.copy(id = nextId.getAndIncrement()) else producto
        products.update { list ->
            if (list.any { it.id == saved.id }) list.map { if (it.id == saved.id) saved else it }
            else list + saved
        }
        saved
    }

    override suspend fun delete(id: Int) = withContext(ioDispatcher) {
        delay(WRITE_DELAY_MS)
        products.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun adjustStock(id: Int, delta: Int): Result<Producto> = withContext(ioDispatcher) {
        delay(WRITE_DELAY_MS)
        var result: Result<Producto> = Result.failure(NoSuchElementException("El producto no existe."))
        // update es atómico: la validación y el cambio de stock no se pisan entre corrutinas.
        products.update { list ->
            val current = list.find { it.id == id }
            result = when {
                current == null -> Result.failure(NoSuchElementException("El producto no existe."))
                current.stockActual + delta < 0 -> Result.failure(
                    IllegalArgumentException("Stock insuficiente: quedan ${current.stockActual} unidades de ${current.nombre}.")
                )
                else -> Result.success(current.copy(stockActual = current.stockActual + delta))
            }
            val updated = result.getOrNull() ?: return@update list
            list.map { if (it.id == id) updated else it }
        }
        result
    }

    override suspend fun replaceCategory(anterior: String, nueva: String) = withContext(ioDispatcher) {
        delay(WRITE_DELAY_MS)
        products.update { list ->
            list.map { if (it.categoria == anterior) it.copy(categoria = nueva) else it }
        }
    }
}
