package com.mauricioliveira.awesomenotes.core.network

import com.mauricioliveira.awesomenotes.core.network.dto.CredentialsDto
import com.mauricioliveira.awesomenotes.core.network.dto.LoginResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApi(private val client: HttpClient) {

    suspend fun register(credentials: CredentialsDto) {
        client.post("auth/register") { setBody(credentials) }
    }

    suspend fun login(credentials: CredentialsDto): LoginResponseDto =
        client.post("auth/login") { setBody(credentials) }.body()

    suspend fun logout() {
        client.post("auth/logout")
    }

    /**
     * Ktor's bearer plugin keeps the token it loaded. Call this after the stored token
     * changes (login, logout) so the next request loads it again.
     */
    fun clearCachedToken() {
        client.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
