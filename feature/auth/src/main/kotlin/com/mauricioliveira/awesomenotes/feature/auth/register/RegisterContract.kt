package com.mauricioliveira.awesomenotes.feature.auth.register

import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.domain.validation.CredentialRules

data class RegisterState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    /** Field errors appear only after the first submit, not while the user is still typing. */
    val submitAttempted: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: AppError? = null,
) {
    val usernameInvalid: Boolean
        get() = submitAttempted && !CredentialRules.isValidUsername(username.trim())

    val passwordInvalid: Boolean
        get() = submitAttempted && !CredentialRules.isValidPassword(password)

    val passwordsDiffer: Boolean
        get() = submitAttempted && confirmPassword != password

    /** The design shows a taken username under the field, not in the banner. */
    val usernameTaken: Boolean
        get() = error == AppError.UsernameTaken

    /** Any other request error, shown in the banner. */
    val formError: AppError?
        get() = error?.takeUnless { it == AppError.UsernameTaken }
}

sealed interface RegisterIntent {
    data class UsernameChanged(val value: String) : RegisterIntent
    data class PasswordChanged(val value: String) : RegisterIntent
    data class ConfirmPasswordChanged(val value: String) : RegisterIntent
    data object Submit : RegisterIntent
}
