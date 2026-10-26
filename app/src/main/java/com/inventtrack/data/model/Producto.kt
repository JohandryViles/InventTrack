package com.inventtrack.data.model

data class Producto(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Double,
    val stockActual: Int,
    val stockMinimo: Int
) {
    val tieneStockBajo: Boolean get() = stockActual <= stockMinimo
}
