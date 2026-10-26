package com.inventtrack.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.model.Usuario
import com.inventtrack.data.repository.AuthRepository
import com.inventtrack.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SettingsDialog { EDIT_PROFILE, CHANGE_PASSWORD, LOGOUT }

data class SettingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    /** null = seguir el tema del sistema. */
    val darkTheme: Boolean? = null,
    val notificationsEnabled: Boolean = true,
    val localSaveEnabled: Boolean = true,
    val dialog: SettingsDialog? = null,
    val profileError: String? = null,
    val passwordError: String? = null,
    val passwordChanged: Boolean = false,
    val isSaving: Boolean = false
)

sealed interface SettingsEvent {
    data object LoggedOut : SettingsEvent
}

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    // Estado propio de la pantalla; el usuario y las preferencias vienen de los repositorios.
    private data class DialogState(
        val dialog: SettingsDialog? = null,
        val profileError: String? = null,
        val passwordError: String? = null,
        val passwordChanged: Boolean = false,
        val isSaving: Boolean = false
    )

    private val dialogState = MutableStateFlow(DialogState())

    val uiState: StateFlow<SettingsUiState> = combine(
        authRepository.currentUser,
        preferencesRepository.darkTheme,
        preferencesRepository.notificationsEnabled,
        preferencesRepository.localSaveEnabled,
        dialogState,
        ::toUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = toUiState(
            authRepository.currentUser.value,
            preferencesRepository.darkTheme.value,
            preferencesRepository.notificationsEnabled.value,
            preferencesRepository.localSaveEnabled.value,
            dialogState.value
        )
    )

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun onDarkThemeChange(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDarkTheme(enabled) }
    }

    fun onNotificationsChange(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setNotificationsEnabled(enabled) }
    }

    fun onLocalSaveChange(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setLocalSaveEnabled(enabled) }
    }

    fun openDialog(dialog: SettingsDialog) {
        dialogState.value = DialogState(dialog = dialog)
    }

    fun dismissDialog() {
        dialogState.value = DialogState()
    }

    fun onDialogInputChange() {
        dialogState.update { it.copy(profileError = null, passwordError = null) }
    }

    fun saveProfile(nombre: String, email: String) {
        val error = when {
            nombre.isBlank() -> "El nombre no puede estar vacio."
            !email.contains("@") || !email.contains(".") -> "Ingresa un correo electronico valido."
            else -> null
        }
        if (error != null) {
            dialogState.update { it.copy(profileError = error) }
            return
        }
        viewModelScope.launch {
            dialogState.update { it.copy(isSaving = true, profileError = null) }
            authRepository.updateProfile(nombre.trim(), email.trim())
                .onSuccess { dialogState.value = DialogState() }
                .onFailure { e -> dialogState.update { it.copy(isSaving = false, profileError = e.message) } }
        }
    }

    fun changePassword(actual: String, nueva: String, confirmacion: String) {
        val error = when {
            actual.isEmpty() -> "Ingresa tu contrasena actual."
            nueva.length < 6 -> "La nueva contrasena debe tener al menos 6 caracteres."
            confirmacion != nueva -> "Las contrasenas no coinciden."
            else -> null
        }
        if (error != null) {
            dialogState.update { it.copy(passwordError = error) }
            return
        }
        viewModelScope.launch {
            dialogState.update { it.copy(isSaving = true, passwordError = null) }
            authRepository.changePassword(actual, nueva)
                .onSuccess { dialogState.update { it.copy(isSaving = false, passwordChanged = true) } }
                .onFailure { e -> dialogState.update { it.copy(isSaving = false, passwordError = e.message) } }
        }
    }

    fun logout() {
        viewModelScope.launch {
            dialogState.value = DialogState()
            authRepository.logout()
            _events.emit(SettingsEvent.LoggedOut)
        }
    }

    private fun toUiState(
        user: Usuario?,
        darkTheme: Boolean?,
        notifications: Boolean,
        localSave: Boolean,
        dialog: DialogState
    ) = SettingsUiState(
        userName = user?.nombre.orEmpty(),
        userEmail = user?.email.orEmpty(),
        darkTheme = darkTheme,
        notificationsEnabled = notifications,
        localSaveEnabled = localSave,
        dialog = dialog.dialog,
        profileError = dialog.profileError,
        passwordError = dialog.passwordError,
        passwordChanged = dialog.passwordChanged,
        isSaving = dialog.isSaving
    )
}
