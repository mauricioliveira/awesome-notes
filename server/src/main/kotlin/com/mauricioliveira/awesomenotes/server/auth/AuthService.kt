package com.mauricioliveira.awesomenotes.server.auth

import com.mauricioliveira.awesomenotes.server.database.SessionRepository
import com.mauricioliveira.awesomenotes.server.database.UserRepository
import com.mauricioliveira.awesomenotes.server.error.InvalidCredentialsException
import com.mauricioliveira.awesomenotes.server.error.UnauthorizedException
import com.mauricioliveira.awesomenotes.server.error.UsernameTakenException
import com.mauricioliveira.awesomenotes.server.error.ValidationException

class AuthService(
    private val users: UserRepository,
    private val sessions: SessionRepository,
    private val passwordHasher: PasswordHasher,
    private val generateToken: () -> String = ::secureRandomToken,
) {

    suspend fun register(username: String, password: String) {
        validate(username, password)
        val created = users.create(username, passwordHasher.hash(password))
        if (!created) throw UsernameTakenException()
    }

    suspend fun login(username: String, password: String): String {
        val user = users.findByUsername(username)
        if (user == null || !passwordHasher.verify(password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }
        val token = generateToken()
        sessions.create(token, user.id)
        return token
    }

    suspend fun logout(token: String) {
        if (!sessions.delete(token)) throw UnauthorizedException()
    }

    suspend fun authenticate(token: String): Long =
        sessions.findUserId(token) ?: throw UnauthorizedException()

    private fun validate(username: String, password: String) {
        if (username.length !in USERNAME_LENGTH) {
            throw ValidationException("Username must be between 3 and 50 characters.")
        }
        if (password.length !in PASSWORD_LENGTH) {
            throw ValidationException("Password must be between 8 and 72 characters.")
        }
    }

    private companion object {
        val USERNAME_LENGTH = 3..50
        val PASSWORD_LENGTH = 8..72
    }
}
