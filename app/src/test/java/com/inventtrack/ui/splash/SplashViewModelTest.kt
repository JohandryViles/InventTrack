package com.inventtrack.ui.splash

import com.inventtrack.data.model.Usuario
import com.inventtrack.data.repository.fake.FakeAuthRepository
import com.inventtrack.data.repository.fake.FakeProductRepository
import com.inventtrack.data.source.MockDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dataSource = MockDataSource()
    private lateinit var auth: FakeAuthRepository
    private lateinit var products: FakeProductRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        auth = FakeAuthRepository(listOf(dataSource.cuentaDemo()), testDispatcher)
        products = FakeProductRepository(dataSource.productos(), testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sinSesion_vaALogin() = runTest(testDispatcher) {
        val viewModel = SplashViewModel(auth, products)

        advanceUntilIdle()

        assertEquals(SplashDestination.LOGIN, viewModel.uiState.value.destination)
        assertEquals(1f, viewModel.uiState.value.progress)
    }

    @Test
    fun conSesion_vaAInicio() = runTest(testDispatcher) {
        auth.startSession(Usuario("Usuario Demo", "demo@inventtrack.com"))
        val viewModel = SplashViewModel(auth, products)

        advanceUntilIdle()

        assertEquals(SplashDestination.HOME, viewModel.uiState.value.destination)
    }

    @Test
    fun cargaElInventarioYEsperaElTiempoMinimo() = runTest(testDispatcher) {
        val viewModel = SplashViewModel(auth, products)

        runCurrent()
        assertEquals("Cargando inventario...", viewModel.uiState.value.message)

        // El repositorio ya respondió, pero todavía no se cumple el tiempo mínimo.
        advanceTimeBy(1_000)
        assertEquals("Inventario listo: 6 productos", viewModel.uiState.value.message)
        assertNull(viewModel.uiState.value.destination)

        advanceTimeBy(SPLASH_MIN_DURATION_MS)
        assertEquals(SplashDestination.LOGIN, viewModel.uiState.value.destination)
    }
}
