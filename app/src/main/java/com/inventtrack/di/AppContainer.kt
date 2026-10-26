package com.inventtrack.di

import com.inventtrack.data.CategoryRepository
import com.inventtrack.data.ProductRepository
import kotlinx.coroutines.Dispatchers

interface AppContainer {
    val productRepository: ProductRepository
    val categoryRepository: CategoryRepository
}

class DefaultAppContainer : AppContainer {
    private val ioDispatcher = Dispatchers.IO
    override val productRepository: ProductRepository by lazy {
        ProductRepository(ioDispatcher)
    }
    override val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(ioDispatcher)
    }
}
