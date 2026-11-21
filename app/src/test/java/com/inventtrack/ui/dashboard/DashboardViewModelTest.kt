package com.inventtrack.ui.dashboard

import com.inventtrack.data.model.TipoMovimiento
import com.inventtrack.data.repository.fake.FakeAuthRepository
import com.inventtrack.data.repository.fake.FakeMovementRepository
import com.inventtrack.data.repository.fake.FakeProductRepository
import com.inventtrack.data.source.MockDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val today = LocalDate.of(2026, 11, 20)
    private val clock = Clock.fixed(today.atTime(12, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val dataSource = MockDataSource(today)

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var productRepository: FakeProductRepository
    private lateinit var movementRepository: FakeMovementRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository(listOf(dataSource.cuentaDemo()), testDispatcher)
        productRepository = FakeProductRepository(dataSource.productos(), testDispatcher)
        movementRepository = FakeMovementRepository(
            seed = dataSource.movimientos(),
            productRepository = productRepository,
            authRepository = authRepository,
            ioDispatcher = testDispatcher
        )
        viewModel = DashboardViewModel(
            authRepository = authRepository,
            productRepository = productRepository,
            movementRepository = movementRepository,
            clock = clock
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.observeUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    @Test
    fun cargaInicial_conSesionActiva_calculaMetricasCorrectamente() = runTest(testDispatcher) {
        observeUiState()
        authRepository.login("demo@inventtrack.com", "demo123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Usuario Demo", state.nombreUsuario)
        assertEquals(6, state.totalProductos)
        // Jarabe pediátrico (stock 2 <= 10) y Azitromicina (stock 5 <= 5)
        assertEquals(2, state.stockBajo)
        // Entradas de hoy: Azitromicina (20) + Jarabe (10) = 30
        assertEquals(30, state.entradasHoy)
        // Salidas de hoy: Suero (5) + Ibuprofeno (4) = 9
        assertEquals(9, state.salidasHoy)
        // Lista limitada a los 5 más recientes
        assertEquals(5, state.movimientosRecientes.size)
        // Las alertas corresponden a los productos con stock bajo
        assertEquals(2, state.alertas.size)
        assertTrue(state.alertas.any { it.contains("Jarabe pediátrico") })
        assertTrue(state.alertas.any { it.contains("Azitromicina 500mg") })
        assertFalse(state.isLoading)
    }

    @Test
    fun sinSesionActiva_nombreUsuarioEstaVacio() = runTest(testDispatcher) {
        observeUiState()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.nombreUsuario)
    }

    @Test
    fun registrarEntrada_actualizaStockBajoYEntradasHoy() = runTest(testDispatcher) {
        observeUiState()
        advanceUntilIdle()

        // Reponer Jarabe pediátrico (id=5) con 20 unidades
        movementRepository.register(
            productoId = 5,
            tipo = TipoMovimiento.ENTRADA,
            cantidad = 20,
            fecha = today.atTime(14, 0),
            nota = "Reposición"
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Jarabe pasa de 2 a 22 unidades (stock mínimo es 10), ya no tiene stock bajo
        assertEquals(1, state.stockBajo)
        assertEquals(1, state.alertas.size)
        assertTrue(state.alertas.first().contains("Azitromicina"))
        // Entradas pasan de 30 a 50
        assertEquals(50, state.entradasHoy)
        // El movimiento reciente más nuevo es la reposición de Jarabe
        assertEquals("Jarabe pediátrico", state.movimientosRecientes.first().productoNombre)
    }

    @Test
    fun eliminarProducto_decrementaTotalProductos() = runTest(testDispatcher) {
        observeUiState()
        advanceUntilIdle()

        assertEquals(6, viewModel.uiState.value.totalProductos)
        productRepository.delete(1) // Paracetamol
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.totalProductos)
    }

    @Test
    fun sinMovimientosDeHoy_entradasYSalidasSonCero() = runTest(testDispatcher) {
        val ayer = today.minusDays(1)
        val repoMovimientosAyer = FakeMovementRepository(
            seed = listOf(
                dataSource.movimientos().last().copy(fecha = ayer.atTime(10, 0))
            ),
            productRepository = productRepository,
            authRepository = authRepository,
            ioDispatcher = testDispatcher
        )
        val vm = DashboardViewModel(authRepository, productRepository, repoMovimientosAyer, clock)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        advanceUntilIdle()

        assertEquals(0, vm.uiState.value.entradasHoy)
        assertEquals(0, vm.uiState.value.salidasHoy)
    }
}
