package com.inventtrack.data.source

import com.inventtrack.data.model.Categoria
import com.inventtrack.data.model.Movimiento
import com.inventtrack.data.model.Producto
import com.inventtrack.data.model.TipoMovimiento
import com.inventtrack.data.model.Usuario
import java.time.LocalDate

/**
 * Datos semilla de la farmacia demo. Es el único lugar con datos de ejemplo:
 * los repositorios fake los copian al iniciar y desde ahí son la fuente de verdad.
 */
class MockDataSource(private val hoy: LocalDate = LocalDate.now()) {

    fun productos(): List<Producto> = listOf(
        Producto(1, "Paracetamol 500mg", "Analgésicos", "Caja con 20 tabletas", 25.0, 60, 10),
        Producto(2, "Ibuprofeno 400mg", "Analgésicos", "Caja con 10 tabletas", 30.0, 18, 10),
        Producto(3, "Azitromicina 500mg", "Antibióticos", "Caja con 3 tabletas", 150.0, 5, 5),
        Producto(4, "Suero fisiológico", "Insumos", "Botella 500ml", 45.0, 45, 10),
        Producto(5, "Jarabe pediátrico", "Jarabes", "Frasco 120ml", 80.0, 2, 10),
        Producto(6, "Amoxicilina 250mg", "Antibióticos", "Caja con 12 cápsulas", 60.0, 30, 8)
    )

    fun categorias(): List<Categoria> = listOf(
        Categoria(1, "Antibióticos"),
        Categoria(2, "Insumos"),
        Categoria(3, "Jarabes"),
        Categoria(4, "Analgésicos")
    )

    fun movimientos(): List<Movimiento> {
        val ayer = hoy.minusDays(1)
        return listOf(
            Movimiento(1, 3, "Azitromicina 500mg", TipoMovimiento.ENTRADA, 20, hoy.atTime(9, 32), "Carlos M.", "Lote nuevo de proveedor."),
            Movimiento(2, 4, "Suero fisiológico", TipoMovimiento.SALIDA, 5, hoy.atTime(10, 15), "Ana R.", "Despacho a sala de atención."),
            Movimiento(3, 5, "Jarabe pediátrico", TipoMovimiento.ENTRADA, 10, hoy.atTime(11, 0), "Carlos M.", "Reposición de stock."),
            Movimiento(4, 2, "Ibuprofeno 400mg", TipoMovimiento.SALIDA, 4, hoy.atTime(12, 30), "Luis G.", "Venta directa de mostrador."),
            Movimiento(5, 6, "Amoxicilina 250mg", TipoMovimiento.ENTRADA, 30, ayer.atTime(15, 20), "Ana R.", "Ingreso de compra mensual."),
            Movimiento(6, 2, "Ibuprofeno 400mg", TipoMovimiento.SALIDA, 12, ayer.atTime(16, 45), "Luis G.", "Entrega de pedido #1042."),
            Movimiento(7, 1, "Paracetamol 500mg", TipoMovimiento.SALIDA, 8, ayer.atTime(17, 30), "Carlos M.", "Despacho regular a farmacia.")
        )
    }

    // Cuenta demo con la que se puede iniciar sesión además del botón de modo demo.
    fun cuentaDemo(): Pair<Usuario, String> = Usuario("Usuario Demo", "demo@inventtrack.com") to "demo123"
}
