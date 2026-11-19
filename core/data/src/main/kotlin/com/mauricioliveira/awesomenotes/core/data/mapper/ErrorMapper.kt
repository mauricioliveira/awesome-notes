package com.mauricioliveira.awesomenotes.core.data.mapper

import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.network.dto.ErrorDto
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs [block] and wraps the outcome in a [Result]. Failures become an [AppError].
 * Cancellation is rethrown, so coroutine cancellation keeps working.
 */
internal suspend inline fun <T> resultOf(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.toAppError())
    }

internal suspend fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is ClientRequestException -> clientError(response.status, errorCode())
    is ServerResponseException -> AppError.Server(response.status.value)
    is IOException -> AppError.NoConnection // no network, DNS failure, timeouts
    else -> AppError.Unknown(this)
}

/** Two cases share a status, so the server's error code tells them apart. */
private fun clientError(status: HttpStatusCode, code: String?): AppError = when (status) {
    HttpStatusCode.Unauthorized ->
        if (code == "INVALID_CREDENTIALS") AppError.InvalidCredentials else AppError.Unauthorized
    HttpStatusCode.Conflict ->
        if (code == "USERNAME_TAKEN") AppError.UsernameTaken else AppError.Server(status.value)
    HttpStatusCode.NotFound -> AppError.NotFound
    else -> AppError.Server(status.value)
}

/** The `code` field of the error body, or null if the body is missing or not an ErrorDto. */
private suspend fun ClientRequestException.errorCode(): String? =
    try {
        response.body<ErrorDto>().code
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
