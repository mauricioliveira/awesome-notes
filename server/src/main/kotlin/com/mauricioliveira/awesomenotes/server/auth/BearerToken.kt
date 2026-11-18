package com.mauricioliveira.awesomenotes.server.auth

import com.mauricioliveira.awesomenotes.server.error.UnauthorizedException
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall

fun ApplicationCall.bearerToken(): String? =
    request.headers[HttpHeaders.Authorization]
        ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
        ?.substring("Bearer ".length)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

suspend fun ApplicationCall.authenticatedUserId(authService: AuthService): Long =
    authService.authenticate(bearerToken() ?: throw UnauthorizedException())
