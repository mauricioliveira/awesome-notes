package com.mauricioliveira.awesomenotes.core.ui.error

import androidx.annotation.StringRes
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.ui.R

@StringRes
fun AppError.messageRes(): Int = when (this) {
    AppError.NoConnection -> R.string.error_no_connection
    AppError.Unauthorized -> R.string.error_unauthorized
    AppError.InvalidCredentials -> R.string.error_invalid_credentials
    AppError.UsernameTaken -> R.string.error_username_taken
    AppError.NotFound -> R.string.error_not_found
    is AppError.Server -> R.string.error_server
    is AppError.Unknown -> R.string.error_unknown
}
