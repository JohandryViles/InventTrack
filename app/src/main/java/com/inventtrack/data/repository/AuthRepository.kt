package com.inventtrack.data.repository

import com.inventtrack.data.model.Usuario
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    /** Usuario con sesión iniciada, o null si no hay sesión. */
    val currentUser: StateFlow<Usuario?>

    suspend fun login(email: String, password: String): Result<Usuario>

    suspend fun register(nombre: String, email: String, password: String): Result<Usuario>

    /** Abre sesión con un usuario ya validado, por ejemplo el botón de modo demo. */
    suspend fun startSession(usuario: Usuario)

    suspend fun updateProfile(nombre: String, email: String): Result<Usuario>

    suspend fun changePassword(actual: String, nueva: String): Result<Unit>

    suspend fun sendPasswordRecovery(email: String): Result<Unit>

    suspend fun logout()
}
