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
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dataSource = MockDataSource()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository(listOf(dataSource.cuentaDemo()), testDispatcher)
        viewModel = RegisterViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun register_conCamposVacios_muestraError() = runTest(testDispatcher) {
        viewModel.register()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("nombre", ignoreCase = true))
    }

    @Test
    fun register_conContrasenasNoCoincidentes_muestraError() = runTest(testDispatcher) {
        viewModel.onNameChange("Andy Garcia")
        viewModel.onEmailChange("andy@inventtrack.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("otra_clave123")
        viewModel.register()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("no coinciden", ignoreCase = true))
    }

    @Test
    fun register_conDatosValidos_registraYEmiteSuccess() = runTest(testDispatcher) {
        var receivedEvent: RegisterEvent? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            receivedEvent = viewModel.events.first()
        }

        viewModel.onNameChange("Andy Garcia")
        viewModel.onEmailChange("andy@inventtrack.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.register()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.isSuccess)
        assertEquals("Andy Garcia", authRepository.currentUser.value?.nombre)

        viewModel.confirmSuccess()
        advanceUntilIdle()

        assertEquals(RegisterEvent.RegisterSuccess, receivedEvent)
    }
}
