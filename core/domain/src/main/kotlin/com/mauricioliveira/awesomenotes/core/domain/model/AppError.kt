package com.mauricioliveira.awesomenotes.core.domain.model

sealed class AppError : Exception() {
    /** No network, DNS failure or timeout. */
    data object NoConnection : AppError()

    /** The session token is missing, invalid or revoked. */
    data object Unauthorized : AppError()

    data object InvalidCredentials : AppError()
    data object UsernameTaken : AppError()
    data object NotFound : AppError()
    data class Server(val code: Int) : AppError()
    data class Unknown(override val cause: Throwable? = null) : AppError()
}

/** For errors that arrive typed as Throwable (Result): keeps an AppError, wraps anything else. */
fun Throwable.asAppError(): AppError = this as? AppError ?: AppError.Unknown(this)
