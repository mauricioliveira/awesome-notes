package com.mauricioliveira.awesomenotes.server.auth

import java.security.SecureRandom

private val secureRandom = SecureRandom()

fun secureRandomToken(): String {
    val bytes = ByteArray(32)
    secureRandom.nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}
