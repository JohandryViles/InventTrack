package com.inventtrack.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class CategoryRepository(private val ioDispatcher: CoroutineDispatcher) {
    private val _categorias = MutableStateFlow(
        listOf(
            Categoria(1, "Antibióticos", ""),
            Categoria(2, "Insumos", ""),
            Categoria(3, "Jarabes", ""),
            Categoria(4, "Analgésicos", "")
        )
    )
    private var nextId = 5

    fun observeCategories(): Flow<List<Categoria>> {
        return _categorias.asStateFlow()
    }

    suspend fun getCategoryById(id: Int): Categoria? = withContext(ioDispatcher) {
        delay(500)
        _categorias.value.find { it.id == id }
    }

    suspend fun addCategory(categoria: Categoria) = withContext(ioDispatcher) {
        delay(500)
        val newCategory = categoria.copy(id = nextId++)
        _categorias.value = _categorias.value + newCategory
    }

    suspend fun updateCategory(categoria: Categoria) = withContext(ioDispatcher) {
        delay(500)
        _categorias.value = _categorias.value.map {
            if (it.id == categoria.id) categoria else it
        }
    }

    suspend fun deleteCategory(id: Int) = withContext(ioDispatcher) {
        delay(500)
        _categorias.value = _categorias.value.filter { it.id != id }
    }
}
