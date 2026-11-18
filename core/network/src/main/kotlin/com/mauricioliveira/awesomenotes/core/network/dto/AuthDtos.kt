package com.mauricioliveira.awesomenotes.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CredentialsDto(val username: String, val password: String)

@Serializable
data class LoginResponseDto(val token: String)

@Serializable
data class ErrorDto(val code: String, val message: String)
