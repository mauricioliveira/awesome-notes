package com.mauricioliveira.awesomenotes.server.auth

import kotlinx.serialization.Serializable

@Serializable
data class CredentialsRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val token: String)
