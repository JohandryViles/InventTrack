package com.inventtrack.ui

import androidx.lifecycle.ViewModel
import com.inventtrack.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.StateFlow

/** Estado global de la Activity: por ahora solo el tema, que Configuración cambia en el repositorio. */
class MainViewModel(
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    /** null = seguir el tema del sistema. */
    val darkTheme: StateFlow<Boolean?> = preferencesRepository.darkTheme
}
