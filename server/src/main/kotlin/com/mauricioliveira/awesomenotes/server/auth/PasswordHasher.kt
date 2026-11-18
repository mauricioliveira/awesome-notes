package com.mauricioliveira.awesomenotes.server.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PasswordHasher(private val cost: Int = 12) {

    suspend fun hash(password: String): String = withContext(Dispatchers.Default) {
        BCrypt.withDefaults().hashToString(cost, password.toCharArray())
    }

    suspend fun verify(password: String, hash: String): Boolean = withContext(Dispatchers.Default) {
        BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }
}
