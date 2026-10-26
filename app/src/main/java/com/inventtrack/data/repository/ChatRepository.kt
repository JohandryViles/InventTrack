package com.inventtrack.data.repository

interface ChatRepository {
    /** Respuesta de InvenBot a [pregunta], calculada con el inventario actual. */
    suspend fun ask(pregunta: String): String
}
