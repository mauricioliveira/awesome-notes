package com.mauricioliveira.awesomenotes.core.network

/** Gives the HTTP client the current session token. Implemented outside this module. */
fun interface AccessTokenProvider {
    suspend fun accessToken(): String?
}
