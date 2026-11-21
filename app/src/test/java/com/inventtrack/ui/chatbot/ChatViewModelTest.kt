package com.inventtrack.ui.chatbot

import com.inventtrack.data.repository.ChatRepository
import com.inventtrack.data.repository.fake.FakeAuthRepository
import com.inventtrack.data.repository.fake.FakeChatRepository
import com.inventtrack.data.repository.fake.FakeMovementRepository
import com.inventtrack.data.repository.fake.FakeProductRepository
import com.inventtrack.data.source.MockDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dataSource = MockDataSource()

    private lateinit var chatRepository: FakeChatRepository
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val products = FakeProductRepository(dataSource.productos(), testDispatcher)
        val auth = FakeAuthRepository(emptyList(), testDispatcher)
        val movements = FakeMovementRepository(dataSource.movimientos(), products, auth, testDispatcher)
        chatRepository = FakeChatRepository(products, movements, testDispatcher)
        viewModel = ChatViewModel(chatRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun estadoInicial_contieneMensajeDeBienvenida_inputVacio_noTyping() {
        val messages = viewModel.messages.value
        assertEquals(1, messages.size)
        assertFalse(messages.first().isUser)
        assertTrue(messages.first().text.contains("InvenBot"))

        assertEquals("", viewModel.input.value)
        assertFalse(viewModel.isTyping.value)
    }

    @Test
    fun onInputChange_actualizaInput() {
        viewModel.onInputChange("¿Stock de Paracetamol?")
        assertEquals("¿Stock de Paracetamol?", viewModel.input.value)
    }

    @Test
    fun send_conTextoDeInput_agregaMensajeUsuario_limpiaInput_yAgregaRespuestaReal() = runTest(testDispatcher) {
        viewModel.onInputChange("¿Qué productos tienen stock bajo?")
        viewModel.send()

        // El input se limpia inmediatamente al enviar
        assertEquals("", viewModel.input.value)

        // Se agregó el mensaje del usuario
        assertEquals(2, viewModel.messages.value.size)
        assertEquals("¿Qué productos tienen stock bajo?", viewModel.messages.value[1].text)
        assertTrue(viewModel.messages.value[1].isUser)

        advanceUntilIdle()

        // Se recibió la respuesta del bot desde el repositorio real
        val messages = viewModel.messages.value
        assertEquals(3, messages.size)
        assertFalse(messages[2].isUser)
        assertTrue(messages[2].text.contains("Jarabe pediátrico"))
        assertTrue(messages[2].text.contains("Azitromicina 500mg"))
        assertFalse(viewModel.isTyping.value)
    }

    @Test
    fun send_conParametroDirecto_agregaMensajeUsuarioYRespuesta() = runTest(testDispatcher) {
        viewModel.send("Stock de Paracetamol")

        assertEquals(2, viewModel.messages.value.size)
        assertEquals("Stock de Paracetamol", viewModel.messages.value[1].text)

        advanceUntilIdle()

        assertEquals(3, viewModel.messages.value.size)
        assertTrue(viewModel.messages.value[2].text.contains("60 unidades"))
        assertFalse(viewModel.isTyping.value)
    }

    @Test
    fun send_conTextoVacioOEspacios_noEnviaNada() = runTest(testDispatcher) {
        viewModel.onInputChange("   ")
        viewModel.send()

        assertEquals(1, viewModel.messages.value.size)
        assertFalse(viewModel.isTyping.value)

        viewModel.send("   ")
        assertEquals(1, viewModel.messages.value.size)
    }

    @Test
    fun send_errorEnRepositorio_muestraMensajeDeErrorAmigable() = runTest(testDispatcher) {
        val failingRepo = object : ChatRepository {
            override suspend fun ask(pregunta: String): String {
                throw RuntimeException("Fallo de red o repositorio")
            }
        }
        val vm = ChatViewModel(failingRepo)
        vm.send("Cualquier pregunta")

        advanceUntilIdle()

        val messages = vm.messages.value
        assertEquals(3, messages.size)
        assertFalse(messages[2].isUser)
        assertTrue(messages[2].text.contains("error"))
        assertFalse(vm.isTyping.value)
    }
}
