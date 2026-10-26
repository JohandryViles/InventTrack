package com.inventtrack.data.model

data class Categoria(
    val id: Int,
    val nombre: String,
    // Calculado por el repositorio a partir de los productos, no se guarda a mano.
    val cantidadProductos: Int = 0
)
