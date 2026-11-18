package com.mauricioliveira.awesomenotes.core.storage

import kotlinx.coroutines.flow.Flow

/** Keeps the session token across app restarts. */
interface TokenStore {

    /** The stored token, or null when logged out. */
    val token: Flow<String?>

    suspend fun save(token: String)

    suspend fun clear()
}
