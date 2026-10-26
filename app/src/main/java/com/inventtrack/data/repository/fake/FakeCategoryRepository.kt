package com.inventtrack.data.repository.fake

import com.inventtrack.data.model.Categoria
import com.inventtrack.data.repository.CategoryRepository
import com.inventtrack.data.repository.ProductRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

class FakeCategoryRepository(
    seed: List<Categoria>,
    private val productRepository: ProductRepository,
    private val ioDispatcher: CoroutineDispatcher
) : CategoryRepository {

    private val categories = MutableStateFlow(seed)
    private val nextId = AtomicInteger((seed.maxOfOrNull { it.id } ?: 0) + 1)

    // El conteo sale de los productos, así nunca se desincroniza del inventario.
    override fun observeCategories(): Flow<List<Categoria>> =
        combine(categories, productRepository.observeProducts()) { list, products ->
            list.map { categoria ->
                categoria.copy(cantidadProductos = products.count { it.categoria == categoria.nombre })
            }
        }.flowOn(ioDispatcher)

    override suspend fun upsert(categoria: Categoria): Result<Categoria> = withContext(ioDispatcher) {
        delay(WRITE_DELAY_MS)
        val nombre = categoria.nombre.trim()
        val current = categories.value
        val previous = current.find { it.id == categoria.id }
        when {
            nombre.isEmpty() ->
                return@withContext Result.failure(IllegalArgumentException("El nombre de la categoría no puede estar vacío."))
            current.any { it.id != categoria.id && it.nombre.equals(nombre, ignoreCase = true) } ->
                return@withContext Result.failure(IllegalArgumentException("Ya existe una categoría llamada $nombre."))
            categoria.id != 0 && previous == null ->
                return@withContext Result.failure(NoSuchElementException("La categoría no existe."))
        }
        val saved = categoria.copy(id = if (categoria.id == 0) nextId.getAndIncrement() else categoria.id, nombre = nombre)
        categories.update { list ->
            if (previous == null) list + saved else list.map { if (it.id == saved.id) saved else it }
        }
        if (previous != null && previous.nombre != nombre) {
            productRepository.replaceCategory(previous.nombre, nombre)
        }
        Result.success(saved)
    }

    override suspend fun delete(id: Int): Result<Unit> = withContext(ioDispatcher) {
        delay(WRITE_DELAY_MS)
        val categoria = categories.value.find { it.id == id }
            ?: return@withContext Result.failure(NoSuchElementException("La categoría no existe."))
        val enUso = productRepository.observeProducts().first().count { it.categoria == categoria.nombre }
        if (enUso > 0) {
            return@withContext Result.failure(
                IllegalStateException("No se puede eliminar ${categoria.nombre}: tiene $enUso productos.")
            )
        }
        categories.update { list -> list.filterNot { it.id == id } }
        Result.success(Unit)
    }
}
