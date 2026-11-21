package com.inventtrack.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.model.ChatMessage
import com.inventtrack.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage("¡Hola! Soy InvenBot. ¿En qué puedo ayudarte con tu inventario hoy?", isUser = false)
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    fun onInputChange(newInput: String) {
        _input.value = newInput
    }

    fun onInputChanged(newInput: String) {
        onInputChange(newInput)
    }

    fun send(pregunta: String? = null) {
        val query = (pregunta ?: _input.value).trim()
        if (query.isBlank() || _isTyping.value) return

        if (pregunta == null) {
            _input.value = ""
        }

        _messages.update { it + ChatMessage(text = query, isUser = true) }
        _isTyping.value = true

        viewModelScope.launch {
            try {
                val respuesta = chatRepository.ask(query)
                _messages.update { it + ChatMessage(text = respuesta, isUser = false) }
            } catch (e: Exception) {
                _messages.update {
                    it + ChatMessage(
                        text = "Lo siento, ocurrió un error al consultar el inventario. Intenta de nuevo.",
                        isUser = false
                    )
                }
            } finally {
                _isTyping.value = false
            }
        }
    }
}
