package com.inventtrack.data.repository.fake

import com.inventtrack.data.model.Usuario
import com.inventtrack.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Autenticación simulada en memoria. Un correo sin cuenta entra en modo demo con un
 * nombre derivado del correo, igual que en la etapa 2; una cuenta registrada exige su contraseña.
 */
class FakeAuthRepository(
    cuentas: List<Pair<Usuario, String>>,
    private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    private data class Cuenta(val usuario: Usuario, val password: String)

    private val accounts = MutableStateFlow(
        cuentas.associate { (usuario, password) -> usuario.email.lowercase() to Cuenta(usuario, password) }
    )
    private val _currentUser = MutableStateFlow<Usuario?>(null)
    override val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): Result<Usuario> = withContext(ioDispatcher) {
        delay(AUTH_DELAY_MS)
        val correo = email.trim()
        val cuenta = accounts.value[correo.lowercase()]
        when {
            cuenta == null -> Result.success(Usuario(nombreDesdeEmail(correo), correo))
            cuenta.password != password -> Result.failure(IllegalArgumentException("Contraseña incorrecta."))
            else -> Result.success(cuenta.usuario)
        }.onSuccess { _currentUser.value = it }
    }

    override suspend fun register(nombre: String, email: String, password: String): Result<Usuario> =
        withContext(ioDispatcher) {
            delay(AUTH_DELAY_MS)
            val usuario = Usuario(nombre.trim(), email.trim())
            val key = usuario.email.lowercase()
            if (accounts.value.containsKey(key)) {
                return@withContext Result.failure(IllegalArgumentException("Ya existe una cuenta con ese correo."))
            }
            accounts.update { it + (key to Cuenta(usuario, password)) }
            _currentUser.value = usuario
            Result.success(usuario)
        }

    override suspend fun startSession(usuario: Usuario) = withContext(ioDispatcher) {
        _currentUser.value = usuario
    }

    override suspend fun updateProfile(nombre: String, email: String): Result<Usuario> = withContext(ioDispatcher) {
        delay(WRITE_DELAY_MS)
        val actual = _currentUser.value
            ?: return@withContext Result.failure(IllegalStateException("No hay una sesión iniciada."))
        val actualizado = Usuario(nombre.trim(), email.trim())
        val oldKey = actual.email.lowercase()
        val newKey = actualizado.email.lowercase()
        if (newKey != oldKey && accounts.value.containsKey(newKey)) {
            return@withContext Result.failure(IllegalArgumentException("Ese correo ya pertenece a otra cuenta."))
        }
        accounts.update { map ->
            val cuenta = map[oldKey] ?: return@update map
            map - oldKey + (newKey to cuenta.copy(usuario = actualizado))
        }
        _currentUser.value = actualizado
        Result.success(actualizado)
    }

    override suspend fun changePassword(actual: String, nueva: String): Result<Unit> = withContext(ioDispatcher) {
        delay(WRITE_DELAY_MS)
        val usuario = _currentUser.value
            ?: return@withContext Result.failure(IllegalStateException("No hay una sesión iniciada."))
        val key = usuario.email.lowercase()
        val cuenta = accounts.value[key]
        if (cuenta != null && cuenta.password != actual) {
            return@withContext Result.failure(IllegalArgumentException("La contraseña actual no es correcta."))
        }
        // Una sesión demo sin cuenta pasa a tener cuenta con la nueva contraseña.
        accounts.update { it + (key to Cuenta(usuario, nueva)) }
        Result.success(Unit)
    }

    override suspend fun sendPasswordRecovery(email: String): Result<Unit> = withContext(ioDispatcher) {
        delay(AUTH_DELAY_MS)
        Result.success(Unit)
    }

    override suspend fun logout() = withContext(ioDispatcher) {
        _currentUser.value = null
    }

    private fun nombreDesdeEmail(email: String): String =
        email.substringBefore("@")
            .split('.', '_', '-')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" ") { it.replaceFirstChar { first -> first.uppercaseChar() } }
            .ifEmpty { "Usuario" }
}
