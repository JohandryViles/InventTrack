package com.inventtrack.ui.settings

import com.inventtrack.data.repository.fake.FakeAuthRepository
import com.inventtrack.data.repository.fake.FakePreferencesRepository
import com.inventtrack.data.source.MockDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var auth: FakeAuthRepository
    private lateinit var preferences: FakePreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        auth = FakeAuthRepository(listOf(MockDataSource().cuentaDemo()), testDispatcher)
        preferences = FakePreferencesRepository(testDispatcher)
        viewModel = SettingsViewModel(auth, preferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // WhileSubscribed solo produce estado mientras alguien lo observa, como hace la pantalla.
    // Unconfined: el colector se suscribe en el acto, antes de que el ViewModel emita.
    private fun TestScope.observeUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun uiState_muestraElUsuarioDeLaSesion() = runTest(testDispatcher) {
        observeUiState()
        auth.login("demo@inventtrack.com", "demo123")
        advanceUntilIdle()

        assertEquals("Usuario Demo", viewModel.uiState.value.userName)
        assertEquals("demo@inventtrack.com", viewModel.uiState.value.userEmail)
    }

    @Test
    fun guardarPerfilConCorreoInvalido_muestraErrorYNoCambiaElUsuario() = runTest(testDispatcher) {
        observeUiState()
        auth.login("demo@inventtrack.com", "demo123")
        viewModel.openDialog(SettingsDialog.EDIT_PROFILE)

        viewModel.saveProfile("Johandry", "correo-invalido")
        advanceUntilIdle()

        assertEquals("Ingresa un correo electronico valido.", viewModel.uiState.value.profileError)
        assertEquals(SettingsDialog.EDIT_PROFILE, viewModel.uiState.value.dialog)
        assertEquals("Usuario Demo", auth.currentUser.value?.nombre)
    }

    @Test
    fun guardarPerfilValido_actualizaElRepositorioYCierraElDialogo() = runTest(testDispatcher) {
        observeUiState()
        auth.login("demo@inventtrack.com", "demo123")
        viewModel.openDialog(SettingsDialog.EDIT_PROFILE)

        viewModel.saveProfile(" Johandry Viles ", "johandry@inventtrack.com")
        advanceUntilIdle()

        assertEquals("Johandry Viles", viewModel.uiState.value.userName)
        assertNull(viewModel.uiState.value.dialog)
    }

    @Test
    fun cambiarContrasena_validaYLuegoConfirma() = runTest(testDispatcher) {
        observeUiState()
        auth.login("demo@inventtrack.com", "demo123")
        viewModel.openDialog(SettingsDialog.CHANGE_PASSWORD)

        viewModel.changePassword("demo123", "nueva123", "otra123")
        advanceUntilIdle()
        assertEquals("Las contrasenas no coinciden.", viewModel.uiState.value.passwordError)

        viewModel.changePassword("incorrecta", "nueva123", "nueva123")
        advanceUntilIdle()
        assertEquals("La contraseña actual no es correcta.", viewModel.uiState.value.passwordError)

        viewModel.changePassword("demo123", "nueva123", "nueva123")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.passwordChanged)
    }

    @Test
    fun modoOscuro_seGuardaEnPreferencias() = runTest(testDispatcher) {
        observeUiState()

        viewModel.onDarkThemeChange(true)
        advanceUntilIdle()

        assertEquals(true, preferences.darkTheme.value)
        assertEquals(true, viewModel.uiState.value.darkTheme)
    }

    @Test
    fun cerrarSesion_limpiaElUsuarioYEmiteElEvento() = runTest(testDispatcher) {
        auth.login("demo@inventtrack.com", "demo123")
        var received: SettingsEvent? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { received = viewModel.events.first() }

        viewModel.logout()
        advanceUntilIdle()

        assertNull(auth.currentUser.value)
        assertEquals(SettingsEvent.LoggedOut, received)
    }
}
