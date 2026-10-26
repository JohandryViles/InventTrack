package com.inventtrack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Estado global de la Activity: por ahora solo el tema. */
class MainViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    /** null = seguir el tema del sistema. */
    val darkTheme: StateFlow<Boolean?> = preferencesRepository.darkTheme

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDarkTheme(enabled) }
    }
}
