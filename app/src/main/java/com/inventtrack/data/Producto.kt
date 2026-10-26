package com.inventtrack.data

data class Producto(
    val id: Int = 0,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Double,
    val stockActual: Int,
    val stockMinimo: Int
)
