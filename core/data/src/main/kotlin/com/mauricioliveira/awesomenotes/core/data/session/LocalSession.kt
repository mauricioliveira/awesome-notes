package com.mauricioliveira.awesomenotes.core.data.session

import com.mauricioliveira.awesomenotes.core.database.dao.NoteDao
import com.mauricioliveira.awesomenotes.core.network.AuthApi
import com.mauricioliveira.awesomenotes.core.storage.TokenStore
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/** Everything the app keeps for the signed-in user: the token and the cached notes. */
internal class LocalSession(
    private val tokenStore: TokenStore,
    private val noteDao: NoteDao,
    private val authApi: AuthApi,
) {

    suspend fun start(token: String, username: String) {
        // No notes from a previous user may survive into this session.
        noteDao.deleteAll()
        tokenStore.save(token, username)
        authApi.clearCachedToken()
    }

    /** Runs to the end even if the caller is cancelled, so no half-cleared session is left. */
    suspend fun clear() = withContext(NonCancellable) {
        tokenStore.clear()
        noteDao.deleteAll()
        authApi.clearCachedToken()
    }
}
