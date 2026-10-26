package com.inventtrack.data.repository.fake

import com.inventtrack.data.model.Movimiento
import com.inventtrack.data.model.TipoMovimiento
import com.inventtrack.data.repository.AuthRepository
import com.inventtrack.data.repository.MovementRepository
import com.inventtrack.data.repository.ProductRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

class FakeMovementRepository(
    seed: List<Movimiento>,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    private val ioDispatcher: CoroutineDispatcher
) : MovementRepository {

    private val movements = MutableStateFlow(seed)
    private val nextId = AtomicInteger((seed.maxOfOrNull { it.id } ?: 0) + 1)

    override fun observeMovements(): Flow<List<Movimiento>> = movements
        .map { list -> list.sortedByDescending { it.fecha } }
        .onStart { delay(READ_DELAY_MS) }
        .flowOn(ioDispatcher)

    override suspend fun register(
        productoId: Int,
        tipo: TipoMovimiento,
        cantidad: Int,
        fecha: LocalDateTime,
        nota: String
    ): Result<Movimiento> = withContext(ioDispatcher) {
        if (cantidad <= 0) {
            return@withContext Result.failure(IllegalArgumentException("La cantidad debe ser mayor que cero."))
        }
        val delta = if (tipo == TipoMovimiento.ENTRADA) cantidad else -cantidad
        // El stock solo cambia en ProductRepository; aquí se guarda el registro del movimiento.
        productRepository.adjustStock(productoId, delta).map { producto ->
            val movimiento = Movimiento(
                id = nextId.getAndIncrement(),
                productoId = producto.id,
                productoNombre = producto.nombre,
                tipo = tipo,
                cantidad = cantidad,
                fecha = fecha,
                usuario = authRepository.currentUser.value?.nombre ?: "Usuario",
                nota = nota.trim()
            )
            movements.update { list -> list + movimiento }
            movimiento
        }
    }
}
