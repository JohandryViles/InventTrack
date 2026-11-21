package com.inventtrack.ui.analytics

import com.inventtrack.data.model.Movimiento
import com.inventtrack.data.model.Producto
import com.inventtrack.data.model.TipoMovimiento
import com.inventtrack.data.repository.fake.FakeAuthRepository
import com.inventtrack.data.repository.fake.FakeMovementRepository
import com.inventtrack.data.repository.fake.FakeProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val today = LocalDate.of(2026, 10, 31)
    private val clock = Clock.fixed(today.atTime(12, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    @Before
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun movement(id: Int, date: String, amount: Int, incoming: Boolean = false, productId: Int = 1) =
        Movimiento(id, productId, "Producto $productId", if (incoming) TipoMovimiento.ENTRADA else TipoMovimiento.SALIDA,
            amount, LocalDate.parse(date).atTime(12, 0), "Usuario")

    private fun TestScope.create(
        seed: List<Movimiento> = emptyList(),
        productSeed: List<Producto> = listOf(Producto(1, "Producto 1", "Categoría A", "", 1.0, 20, 2))
    ): AnalyticsViewModel {
        val products = FakeProductRepository(productSeed, dispatcher)
        val movements = FakeMovementRepository(seed, products, FakeAuthRepository(emptyList(), dispatcher), dispatcher)
        return AnalyticsViewModel(movements, products, clock).also { vm ->
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
            advanceUntilIdle()
        }
    }

    @Test
    fun periods_filterDatesAndIncludeFifthWeekAndYearQuarters() = runTest(dispatcher) {
        val vm = create(listOf(
            movement(1, "2025-12-31", 900),
            movement(2, "2026-01-01", 3),
            movement(3, "2026-04-01", 4),
            movement(4, "2026-07-01", 5),
            movement(5, "2026-10-01", 6),
            movement(6, "2026-10-27", 7),
            movement(7, "2026-10-31", 8, incoming = true),
            movement(8, "2026-11-01", 800)
        ))
        assertEquals(listOf(0, 7, 0, 0, 0, 0, 0), vm.uiState.value.outgoing)
        assertEquals(listOf(0, 0, 0, 0, 0, 8, 0), vm.uiState.value.incoming)

        vm.onPeriodChange(AnalyticsPeriod.MONTH)
        advanceUntilIdle()
        assertEquals(listOf("S1", "S2", "S3", "S4", "S5"), vm.uiState.value.labels)
        assertEquals(listOf(6, 0, 0, 7, 0), vm.uiState.value.outgoing)
        assertEquals(listOf(0, 0, 0, 0, 8), vm.uiState.value.incoming)

        vm.onPeriodChange(AnalyticsPeriod.YEAR)
        advanceUntilIdle()
        assertEquals(listOf(3, 4, 5, 13), vm.uiState.value.outgoing)
        assertEquals("25", vm.uiState.value.totalOutgoing)
        assertEquals("8", vm.uiState.value.totalIncoming)
    }

    @Test
    fun noMovements_hasZeroSeriesAndNoInventedPercentages() = runTest(dispatcher) {
        val vm = create()
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(List(7) { 0 }, vm.uiState.value.incoming)
        assertEquals(List(7) { 0 }, vm.uiState.value.outgoing)
        assertEquals(0, vm.uiState.value.categories.single().percentage)
        assertEquals("0,00 veces", vm.uiState.value.categories.single().rotation)
    }

    @Test
    fun noStock_hasNoDivisionByZero() = runTest(dispatcher) {
        val vm = create(productSeed = listOf(Producto(1, "Vacío", "Categoría A", "", 1.0, 0, 0)))
        assertEquals("Sin base de stock", vm.uiState.value.categories.single().rotation)
    }

    @Test
    fun shares_sumTo100_andIncludeEveryCategoryAndDeletedProducts() = runTest(dispatcher) {
        val vm = create(
            seed = (1..5).map { movement(it, "2026-10-31", 1, productId = it) },
            productSeed = (1..4).map { Producto(it, "Producto $it", "Categoría $it", "", 1.0, 10, 1) }
        )
        val categories = vm.uiState.value.categories
        assertEquals(5, categories.size)
        assertEquals(100, categories.sumOf { it.percentage })
        assertTrue(categories.all { it.percentage == 20 })
        assertTrue(categories.any { it.name == "Sin categoría" })
    }

    @Test
    fun fractionalShares_roundTo100() = runTest(dispatcher) {
        val vm = create(
            seed = (1..3).map { movement(it, "2026-10-31", 1, productId = it) },
            productSeed = (1..3).map { Producto(it, "Producto $it", "Categoría $it", "", 1.0, 10, 1) }
        )
        assertEquals(listOf(34, 33, 33), vm.uiState.value.categories.map { it.percentage })
    }

    @Test
    fun rotation_usesOpeningAndClosingStock() = runTest(dispatcher) {
        val vm = create(listOf(movement(1, "2026-10-31", 10)))
        // Stock final 20, inicial estimado 30: 10 / ((30 + 20) / 2) = 0,4.
        assertEquals("0,40 veces", vm.uiState.value.categories.single().rotation)
    }

    @Test
    fun largeTotals_doNotOverflowChartOrTotals() = runTest(dispatcher) {
        val vm = create(listOf(
            movement(1, "2026-10-31", Int.MAX_VALUE), movement(2, "2026-10-31", Int.MAX_VALUE)
        ))
        assertEquals("4294967294", vm.uiState.value.totalOutgoing)
        assertEquals(Int.MAX_VALUE, vm.uiState.value.outgoing[5])
        assertEquals(100, vm.uiState.value.categories.single().percentage)
    }
}
