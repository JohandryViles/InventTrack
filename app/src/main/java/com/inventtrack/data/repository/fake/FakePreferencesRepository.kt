package com.inventtrack.data.repository.fake

import com.inventtrack.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

// Sin retardo a propósito: los interruptores deben responder al instante.
class FakePreferencesRepository(
    private val ioDispatcher: CoroutineDispatcher
) : PreferencesRepository {

    private val _darkTheme = MutableStateFlow<Boolean?>(null)
    private val _notificationsEnabled = MutableStateFlow(true)
    private val _localSaveEnabled = MutableStateFlow(true)

    override val darkTheme: StateFlow<Boolean?> = _darkTheme.asStateFlow()
    override val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    override val localSaveEnabled: StateFlow<Boolean> = _localSaveEnabled.asStateFlow()

    override suspend fun setDarkTheme(enabled: Boolean) = withContext(ioDispatcher) {
        _darkTheme.value = enabled
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        _notificationsEnabled.value = enabled
    }

    override suspend fun setLocalSaveEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        _localSaveEnabled.value = enabled
    }
}
