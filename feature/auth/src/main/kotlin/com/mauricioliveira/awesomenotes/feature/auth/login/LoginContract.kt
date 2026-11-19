package com.mauricioliveira.awesomenotes.feature.auth.login

import com.mauricioliveira.awesomenotes.core.domain.model.AppError

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: AppError? = null,
) {
    val canSubmit: Boolean
        get() = username.isNotBlank() && password.isNotEmpty() && !isSubmitting
}

sealed interface LoginIntent {
    data class UsernameChanged(val value: String) : LoginIntent
    data class PasswordChanged(val value: String) : LoginIntent
    data object Submit : LoginIntent
}
