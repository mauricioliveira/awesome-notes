package com.mauricioliveira.awesomenotes.server.error

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val code: String, val message: String)
