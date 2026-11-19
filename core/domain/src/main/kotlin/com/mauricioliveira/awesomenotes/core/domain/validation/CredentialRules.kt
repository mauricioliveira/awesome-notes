package com.mauricioliveira.awesomenotes.core.domain.validation

/** The server's limits, checked before any request so the user sees the problem at once. */
object CredentialRules {

    val USERNAME_LENGTH = 3..50
    val PASSWORD_LENGTH = 8..72

    fun isValidUsername(username: String): Boolean = username.length in USERNAME_LENGTH

    fun isValidPassword(password: String): Boolean = password.length in PASSWORD_LENGTH
}
