package com.inventtrack.ui.auth

import com.inventtrack.data.repository.fake.FakeAuthRepository
import com.inventtrack.data.source.MockDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dataSource = MockDataSource()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository(listOf(dataSource.cuentaDemo()), testDispatcher)
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_conCamposVacios_muestraError() = runTest(testDispatcher) {
        viewModel.login()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("correo", ignoreCase = true))
    }

    @Test
    fun login_conCorreoInvalido_muestraError() = runTest(testDispatcher) {
        viewModel.onEmailChange("correoinvalido")
        viewModel.onPasswordChange("123456")
        viewModel.login()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("válido", ignoreCase = true))
    }

    @Test
    fun login_conContrasenaCorta_muestraError() = runTest(testDispatcher) {
        viewModel.onEmailChange("usuario@test.com")
        viewModel.onPasswordChange("123")
        viewModel.login()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("6 caracteres", ignoreCase = true))
    }

    @Test
    fun login_conCredencialesValidas_emiteLoginSuccess() = runTest(testDispatcher) {
        var receivedEvent: LoginEvent? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            receivedEvent = viewModel.events.first()
        }

        viewModel.onEmailChange("demo@inventtrack.com")
        viewModel.onPasswordChange("demo123")
        viewModel.login()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals(LoginEvent.LoginSuccess, receivedEvent)
        assertEquals("Usuario Demo", authRepository.currentUser.value?.nombre)
    }

    @Test
    fun loginDemo_iniciaSesionCorrectamente() = runTest(testDispatcher) {
        var receivedEvent: LoginEvent? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            receivedEvent = viewModel.events.first()
        }

        viewModel.loginDemo()
        advanceUntilIdle()

        assertEquals(LoginEvent.LoginSuccess, receivedEvent)
        assertEquals("demo@inventtrack.com", authRepository.currentUser.value?.email)
    }

    @Test
    fun sendPasswordRecovery_enviaRecuperacionCorrectamente() = runTest(testDispatcher) {
        viewModel.openRecoveryDialog()
        viewModel.onRecoveryEmailChange("usuario@test.com")
        viewModel.sendPasswordRecovery()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.recoverySent)
        assertNull(viewModel.uiState.value.recoveryError)
    }
}
