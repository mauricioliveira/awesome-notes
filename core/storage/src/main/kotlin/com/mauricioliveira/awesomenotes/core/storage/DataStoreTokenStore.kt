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

    override suspend fun save(token: String) {
        dataStore.edit { it[TOKEN_KEY] = token }
    }

    override suspend fun clear() {
        dataStore.edit { it.remove(TOKEN_KEY) }
    }

    private companion object {
        val TOKEN_KEY = stringPreferencesKey("session_token")
    }
}
