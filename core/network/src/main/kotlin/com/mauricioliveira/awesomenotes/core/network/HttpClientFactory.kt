package com.mauricioliveira.awesomenotes.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal object HttpClientFactory {

    fun create(
        engine: HttpClientEngine,
        baseUrl: String,
        tokenProvider: AccessTokenProvider,
        enableLogging: Boolean = false,
    ): HttpClient = HttpClient(engine) {
        expectSuccess = true
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    tokenProvider.accessToken()?.let { BearerTokens(accessToken = it, refreshToken = null) }
                }
                // Send the token on the first request instead of waiting for a 401 challenge.
                sendWithoutRequest { true }
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }
        if (enableLogging) {
            install(Logging) { level = LogLevel.INFO }
        }
    }
}
