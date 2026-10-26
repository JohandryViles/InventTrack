package com.inventtrack.di

import com.inventtrack.data.repository.AuthRepository
import com.inventtrack.data.repository.CategoryRepository
import com.inventtrack.data.repository.ChatRepository
import com.inventtrack.data.repository.MovementRepository
import com.inventtrack.data.repository.PreferencesRepository
import com.inventtrack.data.repository.ProductRepository
import com.inventtrack.data.repository.fake.FakeAuthRepository
import com.inventtrack.data.repository.fake.FakeCategoryRepository
import com.inventtrack.data.repository.fake.FakeChatRepository
import com.inventtrack.data.repository.fake.FakeMovementRepository
import com.inventtrack.data.repository.fake.FakePreferencesRepository
import com.inventtrack.data.repository.fake.FakeProductRepository
import com.inventtrack.data.source.MockDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Contenedor de dependencias manual: el único lugar donde se crean los repositorios. */
interface AppContainer {
    val authRepository: AuthRepository
    val preferencesRepository: PreferencesRepository
    val productRepository: ProductRepository
    val categoryRepository: CategoryRepository
    val movementRepository: MovementRepository
    val chatRepository: ChatRepository
}

class DefaultAppContainer(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val dataSource: MockDataSource = MockDataSource()
) : AppContainer {

    // lazy + una sola instancia por repositorio: todos los ViewModels comparten la misma fuente de verdad.
    override val authRepository: AuthRepository by lazy {
        FakeAuthRepository(listOf(dataSource.cuentaDemo()), ioDispatcher)
    }

    override val preferencesRepository: PreferencesRepository by lazy {
        FakePreferencesRepository(ioDispatcher)
    }

    override val productRepository: ProductRepository by lazy {
        FakeProductRepository(dataSource.productos(), ioDispatcher)
    }

    override val categoryRepository: CategoryRepository by lazy {
        FakeCategoryRepository(dataSource.categorias(), productRepository, ioDispatcher)
    }

    override val movementRepository: MovementRepository by lazy {
        FakeMovementRepository(dataSource.movimientos(), productRepository, authRepository, ioDispatcher)
    }

    override val chatRepository: ChatRepository by lazy {
        FakeChatRepository(productRepository, movementRepository, ioDispatcher)
    }
}
