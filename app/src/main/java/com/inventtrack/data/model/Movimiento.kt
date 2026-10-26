package com.inventtrack.data.model

import java.time.LocalDateTime

enum class TipoMovimiento { ENTRADA, SALIDA }

data class Movimiento(
    val id: Int,
    val productoId: Int,
    val productoNombre: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val fecha: LocalDateTime,
    val usuario: String,
    val nota: String = ""
) {
    val esEntrada: Boolean get() = tipo == TipoMovimiento.ENTRADA

    // Variación de stock: positiva en entradas, negativa en salidas.
    val delta: Int get() = if (esEntrada) cantidad else -cantidad
}
