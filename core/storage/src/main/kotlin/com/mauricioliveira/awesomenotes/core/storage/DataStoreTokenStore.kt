package com.mauricioliveira.awesomenotes.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DataStoreTokenStore(
    private val dataStore: DataStore<Preferences>,
) : TokenStore {

    override val token: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }

    override val username: Flow<String?> = dataStore.data.map { it[USERNAME_KEY] }

    override suspend fun save(token: String, username: String) {
        // One edit, so the token and the username are always written together.
        dataStore.edit {
            it[TOKEN_KEY] = token
            it[USERNAME_KEY] = username
        }
    }

    override suspend fun clear() {
        dataStore.edit {
            it.remove(TOKEN_KEY)
            it.remove(USERNAME_KEY)
        }
    }

    private companion object {
        val TOKEN_KEY = stringPreferencesKey("session_token")
        val USERNAME_KEY = stringPreferencesKey("session_username")
    }
}
