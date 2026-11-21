package com.inventtrack.ui.history

import com.inventtrack.data.model.Movimiento
import com.inventtrack.data.model.TipoMovimiento
import com.inventtrack.data.repository.fake.FakeAuthRepository
import com.inventtrack.data.repository.fake.FakeMovementRepository
import com.inventtrack.data.repository.fake.FakeProductRepository
import com.inventtrack.data.source.MockDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class MovementHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dataSource = MockDataSource()
    private lateinit var movementRepository: FakeMovementRepository
    private lateinit var viewModel: MovementHistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val auth = FakeAuthRepository(listOf(dataSource.cuentaDemo()), testDispatcher)
        val products = FakeProductRepository(dataSource.productos(), testDispatcher)
        movementRepository = FakeMovementRepository(dataSource.movimientos(), products, auth, testDispatcher)
        viewModel = MovementHistoryViewModel(movementRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cargaInicial_agrupaMovimientosPorFecha() = runTest(testDispatcher) {
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.groupedMovements.isNotEmpty())
        assertTrue(state.productOptions.contains("Todos"))
        assertTrue(state.productOptions.size > 1)
        job.cancel()
    }

    @Test
    fun filtroTipo_filtraSoloEntradas() = runTest(testDispatcher) {
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onTypeFilterChange("ENTRADA")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val allFiltered = state.groupedMovements.values.flatten()
        assertTrue(allFiltered.isNotEmpty())
        assertTrue(allFiltered.all { it.tipo == TipoMovimiento.ENTRADA })
        job.cancel()
    }

    @Test
    fun filtroProducto_filtraCorrectamente() = runTest(testDispatcher) {
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val targetProduct = "Azitromicina 500mg"
        viewModel.onProductFilterChange(targetProduct)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val allFiltered = state.groupedMovements.values.flatten()
        assertTrue(allFiltered.isNotEmpty())
        assertTrue(allFiltered.all { it.productoNombre == targetProduct })
        job.cancel()
    }

    @Test
    fun seleccionDetalle_actualizaEstado() = runTest(testDispatcher) {
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        val sample = Movimiento(
            id = 99,
            productoId = 1,
            productoNombre = "Producto Test",
            tipo = TipoMovimiento.ENTRADA,
            cantidad = 10,
            fecha = LocalDateTime.now(),
            usuario = "Tester",
            nota = "Nota test"
        )

        viewModel.selectMovement(sample)
        advanceUntilIdle()

        assertEquals(sample, viewModel.uiState.value.selectedMovement)

        viewModel.dismissDetail()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedMovement)
        job.cancel()
    }
}
