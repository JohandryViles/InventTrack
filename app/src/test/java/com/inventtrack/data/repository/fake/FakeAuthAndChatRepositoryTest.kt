package com.inventtrack.data.repository.fake

import com.inventtrack.data.source.MockDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FakeAuthAndChatRepositoryTest {

    private val dataSource = MockDataSource()

    @Test
    fun loginCuentaDemo_conContrasenaIncorrecta_falla() = runTest {
        val auth = FakeAuthRepository(listOf(dataSource.cuentaDemo()), StandardTestDispatcher(testScheduler))

        assertTrue(auth.login("demo@inventtrack.com", "otra123").isFailure)
        assertNull(auth.currentUser.value)
        assertTrue(auth.login("DEMO@inventtrack.com", "demo123").isSuccess)
        assertEquals("Usuario Demo", auth.currentUser.value?.nombre)
    }

    @Test
    fun loginCorreoSinCuenta_entraEnModoDemoConNombreDelCorreo() = runTest {
        val auth = FakeAuthRepository(emptyList(), StandardTestDispatcher(testScheduler))

        auth.login("ana.rodriguez@farmacia.com", "123456")

        assertEquals("Ana Rodriguez", auth.currentUser.value?.nombre)
    }

    @Test
    fun actualizarPerfilYCerrarSesion_cambianLaSesion() = runTest {
        val auth = FakeAuthRepository(listOf(dataSource.cuentaDemo()), StandardTestDispatcher(testScheduler))
        auth.login("demo@inventtrack.com", "demo123")

        auth.updateProfile("Johandry Viles", "johandry@inventtrack.com")
        assertEquals("johandry@inventtrack.com", auth.currentUser.value?.email)

        auth.logout()
        assertNull(auth.currentUser.value)
        // La cuenta se movió al correo nuevo con la misma contraseña.
        assertTrue(auth.login("johandry@inventtrack.com", "demo123").isSuccess)
    }

    @Test
    fun cambiarContrasena_validaLaActual() = runTest {
        val auth = FakeAuthRepository(listOf(dataSource.cuentaDemo()), StandardTestDispatcher(testScheduler))
        auth.login("demo@inventtrack.com", "demo123")

        assertTrue(auth.changePassword("incorrecta", "nueva123").isFailure)
        assertTrue(auth.changePassword("demo123", "nueva123").isSuccess)
        auth.logout()
        assertFalse(auth.login("demo@inventtrack.com", "demo123").isSuccess)
    }

    @Test
    fun registrarCorreoExistente_falla() = runTest {
        val auth = FakeAuthRepository(listOf(dataSource.cuentaDemo()), StandardTestDispatcher(testScheduler))

        assertTrue(auth.register("Otro", "demo@inventtrack.com", "123456").isFailure)
    }

    @Test
    fun chatbot_respondeConDatosDelInventario() = runTest {
        val io = StandardTestDispatcher(testScheduler)
        val products = FakeProductRepository(dataSource.productos(), io)
        val movements = FakeMovementRepository(
            dataSource.movimientos(), products, FakeAuthRepository(emptyList(), io), io
        )
        val chat = FakeChatRepository(products, movements, io)

        val stockBajo = chat.ask("¿Qué productos tienen stock bajo?")
        assertTrue(stockBajo.contains("Jarabe pediátrico") && stockBajo.contains("Azitromicina 500mg"))
        assertTrue(chat.ask("Stock de Paracetamol").contains("60 unidades"))
        assertTrue(chat.ask("¿Qué entradas hubo hoy?").contains("2 entradas"))
    }
}
