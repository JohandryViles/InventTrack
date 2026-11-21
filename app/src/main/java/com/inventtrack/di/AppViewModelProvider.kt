package com.inventtrack.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventtrack.InventTrackApplication
import com.inventtrack.ui.MainViewModel
import com.inventtrack.ui.analytics.AnalyticsViewModel
import com.inventtrack.ui.movements.MovementViewModel
import com.inventtrack.ui.products.ProductStockViewModel
import com.inventtrack.ui.session.SessionViewModel
import com.inventtrack.ui.settings.SettingsViewModel
import com.inventtrack.ui.splash.SplashViewModel

/**
 * Fábrica única de ViewModels. Cada ViewModel recibe sus repositorios por constructor
 * desde el [AppContainer]; ningún ViewModel crea sus dependencias.
 *
 * Para agregar uno nuevo: `initializer { MiViewModel(inventTrackContainer().miRepository) }`
 * y en la pantalla `viewModel(factory = AppViewModelProvider.Factory)`.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { ProductStockViewModel(inventTrackContainer().productRepository) }
        initializer {
            val container = inventTrackContainer()
            MovementViewModel(container.productRepository, container.movementRepository)
        }
        initializer {
            val container = inventTrackContainer()
            AnalyticsViewModel(container.movementRepository, container.productRepository)
        }
        initializer { MainViewModel(inventTrackContainer().preferencesRepository) }
        initializer { SessionViewModel(inventTrackContainer().authRepository) }
        initializer {
            val container = inventTrackContainer()
            SplashViewModel(container.authRepository, container.productRepository)
        }
        initializer {
            val container = inventTrackContainer()
            SettingsViewModel(container.authRepository, container.preferencesRepository)
        }
    }
}

fun CreationExtras.inventTrackContainer(): AppContainer =
    (this[APPLICATION_KEY] as InventTrackApplication).container
