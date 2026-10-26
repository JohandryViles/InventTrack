package com.inventtrack.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.model.Usuario
import com.inventtrack.data.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Sesión compartida por la navegación. Reemplaza el userName/userEmail que vivía en
 * AppNavigation: el usuario ahora sale de [AuthRepository].
 */
class SessionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<Usuario?> = authRepository.currentUser

    // Puente para Login y Registro mientras no tengan su propio ViewModel.
    fun startSession(nombre: String, email: String) {
        viewModelScope.launch { authRepository.startSession(Usuario(nombre, email)) }
    }
}
