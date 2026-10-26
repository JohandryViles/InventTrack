package com.inventtrack.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventtrack.InventTrackApplication
import com.inventtrack.ui.MainViewModel
import com.inventtrack.ui.session.SessionViewModel

/**
 * Fábrica única de ViewModels. Cada ViewModel recibe sus repositorios por constructor
 * desde el [AppContainer]; ningún ViewModel crea sus dependencias.
 *
 * Para agregar uno nuevo: `initializer { MiViewModel(inventTrackContainer().miRepository) }`
 * y en la pantalla `viewModel(factory = AppViewModelProvider.Factory)`.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { MainViewModel(inventTrackContainer().preferencesRepository) }
        initializer { SessionViewModel(inventTrackContainer().authRepository) }
    }
}

fun CreationExtras.inventTrackContainer(): AppContainer =
    (this[APPLICATION_KEY] as InventTrackApplication).container
