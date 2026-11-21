package com.inventtrack.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val showRecoveryDialog: Boolean = false,
    val recoveryEmail: String = "",
    val recoverySent: Boolean = false,
    val isRecoveryLoading: Boolean = false,
    val recoveryError: String? = null
)

sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        val validationError = when {
            email.isEmpty() -> "Por favor ingresa tu correo electrónico."
            !email.contains("@") || !email.contains(".") -> "Ingresa un correo electrónico válido."
            password.isEmpty() -> "Por favor ingresa tu contraseña."
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            else -> null
        }

        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email, password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(LoginEvent.LoginSuccess)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al iniciar sesión."
                        )
                    }
                }
        }
    }

    fun loginDemo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login("demo@inventtrack.com", "demo123")
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(LoginEvent.LoginSuccess)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al iniciar sesión en modo demo."
                        )
                    }
                }
        }
    }

    fun openRecoveryDialog() {
        val currentEmail = _uiState.value.email.trim()
        _uiState.update {
            it.copy(
                showRecoveryDialog = true,
                recoveryEmail = currentEmail,
                recoverySent = false,
                recoveryError = null
            )
        }
    }

    fun dismissRecoveryDialog() {
        _uiState.update {
            it.copy(
                showRecoveryDialog = false,
                recoverySent = false,
                recoveryError = null,
                isRecoveryLoading = false
            )
        }
    }

    fun onRecoveryEmailChange(email: String) {
        _uiState.update { it.copy(recoveryEmail = email, recoveryError = null) }
    }

    fun sendPasswordRecovery() {
        val email = _uiState.value.recoveryEmail.trim()
        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            _uiState.update { it.copy(recoveryError = "Ingresa un correo electrónico válido.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isRecoveryLoading = true, recoveryError = null) }
            authRepository.sendPasswordRecovery(email)
                .onSuccess {
                    _uiState.update { it.copy(isRecoveryLoading = false, recoverySent = true) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isRecoveryLoading = false,
                            recoveryError = exception.message ?: "Error al enviar recuperación."
                        )
                    }
                }
        }
    }
}
