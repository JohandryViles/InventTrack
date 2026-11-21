package com.inventtrack.ui.movements

import com.inventtrack.data.model.Movimiento
import com.inventtrack.data.model.TipoMovimiento
import com.inventtrack.data.repository.MovementRepository
import com.inventtrack.data.repository.fake.FakeAuthRepository
import com.inventtrack.data.repository.fake.FakeMovementRepository
import com.inventtrack.data.repository.fake.FakeProductRepository
import com.inventtrack.data.source.MockDataSource
import com.inventtrack.ui.analytics.AnalyticsViewModel
import com.inventtrack.ui.products.ProductStockViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class MovementViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var products: FakeProductRepository
    private lateinit var movements: FakeMovementRepository
    private lateinit var viewModel: MovementViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val source = MockDataSource()
        products = FakeProductRepository(source.productos(), dispatcher)
        movements = FakeMovementRepository(emptyList(), products, FakeAuthRepository(emptyList(), dispatcher), dispatcher)
        viewModel = MovementViewModel(products, movements)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun TestScope.observe() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()
    }

    @Test
    fun invalidQuantities_doNotChangeStockOrRegister() = runTest(dispatcher) {
        observe()
        viewModel.onProductoChange(1)
        for (quantity in listOf("", " ", "0", "-1", "texto", "1.5", "99999999999999999999")) {
            viewModel.onCantidadChange(quantity)
            viewModel.registrar()
            advanceUntilIdle()
            assertNotNull("Debe rechazar '$quantity'", viewModel.uiState.value.error)
            assertFalse(viewModel.uiState.value.isSaving)
        }
        assertEquals(60, products.getProduct(1)?.stockActual)
        assertTrue(movements.observeMovements().first().isEmpty())
    }

    @Test
    fun missingOrDeletedProduct_isRejected() = runTest(dispatcher) {
        observe()
        viewModel.onCantidadChange("1")
        viewModel.registrar()
        advanceUntilIdle()
        assertEquals("Selecciona un producto.", viewModel.uiState.value.error)

        viewModel.onProductoChange(1)
        products.delete(1)
        viewModel.registrar()
        advanceUntilIdle()
        assertEquals("El producto ya no existe. Selecciona otro.", viewModel.uiState.value.error)
    }

    @Test
    fun excessiveExitAndOverflowingEntry_areRejected() = runTest(dispatcher) {
        observe()
        viewModel.onProductoChange(5)
        viewModel.onTipoChange(TipoMovimiento.SALIDA)
        viewModel.onCantidadChange("3")
        viewModel.registrar()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)
        assertEquals(2, products.getProduct(5)?.stockActual)

        viewModel.onTipoChange(TipoMovimiento.ENTRADA)
        viewModel.onCantidadChange(Int.MAX_VALUE.toString())
        viewModel.registrar()
        advanceUntilIdle()
        assertEquals("La cantidad supera el límite permitido.", viewModel.uiState.value.error)
        assertEquals(2, products.getProduct(5)?.stockActual)
    }

    @Test
    fun exit_updatesProductsAndAnalytics_andDoubleTapRegistersOnlyOnce() = runTest(dispatcher) {
        observe()
        val stockList = ProductStockViewModel(products)
        val analytics = AnalyticsViewModel(movements, products)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { stockList.uiState.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { analytics.uiState.collect {} }
        advanceUntilIdle()
        assertEquals("0", analytics.uiState.value.totalOutgoing)

        viewModel.onProductoChange(1)
        viewModel.onTipoChange(TipoMovimiento.SALIDA)
        viewModel.onCantidadChange("10")
        viewModel.onNotaChange(" Venta ")
        viewModel.registrar()
        viewModel.registrar()
        runCurrent()
        assertTrue(viewModel.uiState.value.isSaving)
        advanceUntilIdle()

        assertEquals(50, stockList.uiState.value.products.first { it.id == 1 }.stockActual)
        assertEquals(50, viewModel.uiState.value.producto?.stockActual)
        assertEquals("10", analytics.uiState.value.totalOutgoing)
        assertEquals(10, analytics.uiState.value.outgoing.sum())
        val category = analytics.uiState.value.categories.first { it.name == "Analgésicos" }
        assertEquals(100, category.percentage)
        assertEquals("0,14 veces", category.rotation)
        val saved = movements.observeMovements().first().single()
        assertEquals("Venta", saved.nota)
        assertEquals(LocalDate.now(), saved.fecha.toLocalDate())
        assertEquals("", viewModel.uiState.value.cantidad)
        assertNotNull(viewModel.uiState.value.confirmacion)
        assertFalse(viewModel.uiState.value.isSaving)
        viewModel.dismissConfirmation()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.confirmacion)
    }

    @Test
    fun repositoryFailure_keepsFormAndReleasesSaving() = runTest(dispatcher) {
        val failing = object : MovementRepository {
            override fun observeMovements(): Flow<List<Movimiento>> = flowOf(emptyList())
            override suspend fun register(productoId: Int, tipo: TipoMovimiento, cantidad: Int,
                fecha: LocalDateTime, nota: String): Result<Movimiento> = Result.failure(IllegalStateException("Sin conexión"))
        }
        viewModel = MovementViewModel(products, failing)
        observe()
        viewModel.onProductoChange(1)
        viewModel.onCantidadChange("5")
        viewModel.registrar()
        advanceUntilIdle()
        assertEquals("Sin conexión", viewModel.uiState.value.error)
        assertEquals("5", viewModel.uiState.value.cantidad)
        assertFalse(viewModel.uiState.value.isSaving)
        assertNull(viewModel.uiState.value.confirmacion)
    }

    @Test
    fun entry_usesSelectedDateAndIncreasesSharedStock() = runTest(dispatcher) {
        observe()
        val date = LocalDate.of(2026, 1, 15)
        viewModel.onProductoChange(5)
        viewModel.onCantidadChange("4")
        viewModel.onFechaChange(date)
        viewModel.registrar()
        advanceUntilIdle()
        assertEquals(6, products.getProduct(5)?.stockActual)
        assertEquals(date, movements.observeMovements().first().single().fecha.toLocalDate())
    }
}
