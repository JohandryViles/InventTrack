package com.inventtrack.data.repository.fake

import com.inventtrack.data.model.Categoria
import com.inventtrack.data.model.Producto
import com.inventtrack.data.model.TipoMovimiento
import com.inventtrack.data.source.MockDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class FakeInventoryRepositoriesTest {

    private val dataSource = MockDataSource()

    // El dispatcher se inyecta: en pruebas los delay() avanzan en tiempo virtual.
    private fun TestScope.repositories(): Triple<FakeProductRepository, FakeCategoryRepository, FakeMovementRepository> {
        val io = StandardTestDispatcher(testScheduler)
        val auth = FakeAuthRepository(listOf(dataSource.cuentaDemo()), io)
        val products = FakeProductRepository(dataSource.productos(), io)
        val categories = FakeCategoryRepository(dataSource.categorias(), products, io)
        val movements = FakeMovementRepository(dataSource.movimientos(), products, auth, io)
        return Triple(products, categories, movements)
    }

    private suspend fun FakeProductRepository.stockOf(id: Int) =
        observeProducts().first().first { it.id == id }.stockActual

    @Test
    fun adjustStock_quedaNegativo_fallaYNoCambiaElStock() = runTest {
        val (products) = repositories()

        val result = products.adjustStock(id = 5, delta = -3) // Jarabe pediátrico tiene 2

        assertTrue(result.isFailure)
        assertEquals(2, products.stockOf(5))
    }

    @Test
    fun upsert_productoNuevo_asignaIdYLoPublica() = runTest {
        val (products) = repositories()

        val saved = products.upsert(Producto(0, "Loratadina 10mg", "Antialérgicos", "Caja con 10", 12.0, 20, 5))

        assertEquals(7, saved.id)
        assertEquals(saved, products.observeProducts().first().last())
    }

    @Test
    fun registrarSalida_descuentaStockEnElRepositorioDeProductos() = runTest {
        val (products, _, movements) = repositories()

        val result = movements.register(1, TipoMovimiento.SALIDA, 10, LocalDateTime.now(), " Venta ")

        assertTrue(result.isSuccess)
        assertEquals(50, products.stockOf(1))
        val ultimo = movements.observeMovements().first().first()
        assertEquals("Paracetamol 500mg", ultimo.productoNombre)
        assertEquals("Venta", ultimo.nota)
    }

    @Test
    fun registrarSalidaMayorAlStock_noGuardaElMovimiento() = runTest {
        val (products, _, movements) = repositories()
        val antes = movements.observeMovements().first().size

        val result = movements.register(3, TipoMovimiento.SALIDA, 6, LocalDateTime.now(), "")

        assertTrue(result.isFailure)
        assertEquals(antes, movements.observeMovements().first().size)
        assertEquals(5, products.stockOf(3))
    }

    @Test
    fun registrarCantidadCero_falla() = runTest {
        val (_, _, movements) = repositories()

        assertTrue(movements.register(1, TipoMovimiento.ENTRADA, 0, LocalDateTime.now(), "").isFailure)
    }

    @Test
    fun categorias_cuentanLosProductosActuales() = runTest {
        val (_, categories) = repositories()

        val antibioticos = categories.observeCategories().first().first { it.nombre == "Antibióticos" }

        assertEquals(2, antibioticos.cantidadProductos)
    }

    @Test
    fun renombrarCategoria_actualizaSusProductos() = runTest {
        val (products, categories) = repositories()

        categories.upsert(Categoria(id = 3, nombre = "Jarabes y suspensiones"))

        assertEquals("Jarabes y suspensiones", products.observeProducts().first().first { it.id == 5 }.categoria)
    }

    @Test
    fun categoriaRepetidaOVacia_falla() = runTest {
        val (_, categories) = repositories()

        assertTrue(categories.upsert(Categoria(0, "insumos")).isFailure)
        assertTrue(categories.upsert(Categoria(0, "  ")).isFailure)
    }

    @Test
    fun eliminarCategoriaConProductos_falla() = runTest {
        val (_, categories) = repositories()

        assertTrue(categories.delete(id = 4).isFailure)
        assertTrue(categories.upsert(Categoria(0, "Vacunas")).isSuccess)
        assertTrue(categories.delete(id = 5).isSuccess)
    }
}
