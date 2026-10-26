package com.inventtrack.data.repository

import com.inventtrack.data.model.Movimiento
import com.inventtrack.data.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MovementRepository {
    /** Movimientos ordenados del más reciente al más antiguo. */
    fun observeMovements(): Flow<List<Movimiento>>

    /**
     * Registra el movimiento y actualiza el stock en [ProductRepository].
     * Falla si el producto no existe, la cantidad no es positiva o la salida supera el stock.
     */
    suspend fun register(
        productoId: Int,
        tipo: TipoMovimiento,
        cantidad: Int,
        fecha: LocalDateTime,
        nota: String
    ): Result<Movimiento>
}
