package com.mauricioliveira.awesomenotes.core.storage.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.mauricioliveira.awesomenotes.core.storage.DataStoreTokenStore
import com.mauricioliveira.awesomenotes.core.storage.TokenStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// The delegate guarantees a single DataStore instance per file, as DataStore requires.
private val Context.sessionDataStore by preferencesDataStore(name = "session")

val storageModule = module {
    single<TokenStore> { DataStoreTokenStore(androidContext().sessionDataStore) }
}
