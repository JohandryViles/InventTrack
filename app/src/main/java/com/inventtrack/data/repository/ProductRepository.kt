package com.inventtrack.data.repository

import com.inventtrack.data.model.Producto
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<Producto>>

    fun observeProduct(id: Int): Flow<Producto?>

    suspend fun getProduct(id: Int): Producto?

    /** Crea el producto si su id es 0; si no, lo reemplaza. Devuelve el producto guardado. */
    suspend fun upsert(producto: Producto): Producto

    suspend fun delete(id: Int)

    /** Suma [delta] al stock. Falla si el producto no existe o si el stock quedaría negativo. */
    suspend fun adjustStock(id: Int, delta: Int): Result<Producto>

    /** Cambia la categoría de todos los productos que usan [anterior]. */
    suspend fun replaceCategory(anterior: String, nueva: String)
}
