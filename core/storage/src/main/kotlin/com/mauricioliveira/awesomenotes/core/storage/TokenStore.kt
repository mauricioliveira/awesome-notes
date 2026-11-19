package com.mauricioliveira.awesomenotes.core.storage

import kotlinx.coroutines.flow.Flow

/** Keeps the session token, and the username it belongs to, across app restarts. */
interface TokenStore {

    /** The stored token, or null when logged out. */
    val token: Flow<String?>

    /** The username of the stored session, or null when logged out. Only for display. */
    val username: Flow<String?>

    suspend fun save(token: String, username: String)

    suspend fun clear()
}
