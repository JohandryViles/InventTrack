package com.inventtrack.data.repository

import com.inventtrack.data.model.Categoria
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    /** Categorías con [Categoria.cantidadProductos] calculado desde los productos actuales. */
    fun observeCategories(): Flow<List<Categoria>>

    /** Crea la categoría si su id es 0; si no, la renombra. Falla si el nombre está vacío o repetido. */
    suspend fun upsert(categoria: Categoria): Result<Categoria>

    /** Falla si todavía hay productos en la categoría. */
    suspend fun delete(id: Int): Result<Unit>
}
