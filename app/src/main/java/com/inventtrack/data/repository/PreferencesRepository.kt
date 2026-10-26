package com.inventtrack.data.repository

import kotlinx.coroutines.flow.StateFlow

interface PreferencesRepository {
    /** null significa seguir el tema del sistema. */
    val darkTheme: StateFlow<Boolean?>
    val notificationsEnabled: StateFlow<Boolean>
    val localSaveEnabled: StateFlow<Boolean>

    suspend fun setDarkTheme(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setLocalSaveEnabled(enabled: Boolean)
}
