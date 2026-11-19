package com.mauricioliveira.awesomenotes.feature.auth.register

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauricioliveira.awesomenotes.core.domain.model.asAppError
import com.mauricioliveira.awesomenotes.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    // Only the username survives process death. Passwords are never written to saved state.
    private val _state = MutableStateFlow(RegisterState(username = savedStateHandle[USERNAME_KEY] ?: ""))
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    /** Set once the account exists, so a retry after a failed login does not register again. */
    private var registeredUsername: String? = null

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.UsernameChanged -> {
                savedStateHandle[USERNAME_KEY] = intent.value
                _state.update { it.copy(username = intent.value, error = null) }
            }
            is RegisterIntent.PasswordChanged -> _state.update { it.copy(password = intent.value, error = null) }
            is RegisterIntent.ConfirmPasswordChanged -> _state.update { it.copy(confirmPassword = intent.value, error = null) }
            RegisterIntent.Submit -> submit()
        }
    }

    private fun submit() {
        if (_state.value.isSubmitting) return
        val current = _state.updateAndGet { it.copy(submitAttempted = true) }
        if (current.usernameInvalid || current.passwordInvalid || current.passwordsDiffer) return
        _state.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            val username = current.username.trim()
            val registered = registerOnce(username, current.password)
            // Logs in straight away (ADR-021). On success the app switches to the notes.
            val result = if (registered.isSuccess) authRepository.login(username, current.password) else registered
            result.onFailure { error ->
                _state.update { it.copy(isSubmitting = false, error = error.asAppError()) }
            }
        }
    }

    /** Skips the register call when this username was already registered by an earlier attempt. */
    private suspend fun registerOnce(username: String, password: String): Result<Unit> {
        if (registeredUsername == username) return Result.success(Unit)
        return authRepository.register(username, password).onSuccess { registeredUsername = username }
    }

    private companion object {
        const val USERNAME_KEY = "username"
    }
}
