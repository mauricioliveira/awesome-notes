package com.mauricioliveira.awesomenotes.server.error

import io.ktor.http.HttpStatusCode

sealed class ApiException(
    val status: HttpStatusCode,
    val code: String,
    override val message: String,
) : RuntimeException(message)

class ValidationException(message: String) :
    ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", message)

class UsernameTakenException :
    ApiException(HttpStatusCode.Conflict, "USERNAME_TAKEN", "This username is already taken.")

class InvalidCredentialsException :
    ApiException(HttpStatusCode.Unauthorized, "INVALID_CREDENTIALS", "Wrong username or password.")

class UnauthorizedException :
    ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Authentication required.")
