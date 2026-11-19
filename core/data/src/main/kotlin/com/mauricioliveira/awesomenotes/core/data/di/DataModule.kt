package com.mauricioliveira.awesomenotes.core.data.di

import com.mauricioliveira.awesomenotes.core.data.repository.DefaultAuthRepository
import com.mauricioliveira.awesomenotes.core.data.repository.DefaultNoteRepository
import com.mauricioliveira.awesomenotes.core.data.session.LocalSession
import com.mauricioliveira.awesomenotes.core.data.session.StoredAccessTokenProvider
import com.mauricioliveira.awesomenotes.core.domain.repository.AuthRepository
import com.mauricioliveira.awesomenotes.core.domain.repository.NoteRepository
import com.mauricioliveira.awesomenotes.core.network.AccessTokenProvider
import org.koin.dsl.module

/** Needs networkModule, storageModule and databaseModule, which the app also loads. */
val dataModule = module {
    single<AccessTokenProvider> { StoredAccessTokenProvider(tokenStore = get()) }
    single { LocalSession(tokenStore = get(), noteDao = get(), authApi = get()) }
    single<AuthRepository> { DefaultAuthRepository(authApi = get(), tokenStore = get(), session = get()) }
    single<NoteRepository> { DefaultNoteRepository(notesApi = get(), noteDao = get(), session = get()) }
}
