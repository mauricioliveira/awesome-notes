package com.mauricioliveira.awesomenotes.feature.auth.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauricioliveira.awesomenotes.core.domain.model.asAppError
import com.mauricioliveira.awesomenotes.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    // Only the username survives process death. The password is never written to saved state.
    private val _state = MutableStateFlow(LoginState(username = savedStateHandle[USERNAME_KEY] ?: ""))
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UsernameChanged -> {
                savedStateHandle[USERNAME_KEY] = intent.value
                _state.update { it.copy(username = intent.value, error = null) }
            }
            is LoginIntent.PasswordChanged -> _state.update { it.copy(password = intent.value, error = null) }
            LoginIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            authRepository.login(current.username.trim(), current.password)
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false, error = error.asAppError()) }
                }
            // On success the session starts and the app switches to the notes: nothing to do here.
        }
    }

    private companion object {
        const val USERNAME_KEY = "username"
    }
}
