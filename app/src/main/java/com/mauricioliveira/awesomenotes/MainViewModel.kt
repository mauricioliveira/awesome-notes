package com.mauricioliveira.awesomenotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauricioliveira.awesomenotes.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SessionState {
    /** The stored token has not been read yet. */
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data object LoggedIn : SessionState
}

class MainViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val sessionState: StateFlow<SessionState> =
        authRepository.isLoggedIn
            .map { loggedIn -> if (loggedIn) SessionState.LoggedIn else SessionState.LoggedOut }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SessionState.Loading,
            )

    // Temporary: the notes list will own logout once feature:notes exists.
    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
