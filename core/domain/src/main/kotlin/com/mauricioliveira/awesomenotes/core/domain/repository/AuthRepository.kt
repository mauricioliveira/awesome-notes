package com.mauricioliveira.awesomenotes.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /** Emits true while a session token is stored. */
    val isLoggedIn: Flow<Boolean>

    /** The logged-in username, or null when logged out. */
    val username: Flow<String?>

    suspend fun register(username: String, password: String): Result<Unit>

    suspend fun login(username: String, password: String): Result<Unit>

    /** Always ends the local session, even if the server cannot be reached. */
    suspend fun logout()
}
