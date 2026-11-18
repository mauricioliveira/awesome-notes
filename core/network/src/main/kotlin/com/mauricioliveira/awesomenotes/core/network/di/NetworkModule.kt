package com.mauricioliveira.awesomenotes.core.network.di

import com.mauricioliveira.awesomenotes.core.network.AuthApi
import com.mauricioliveira.awesomenotes.core.network.BuildConfig
import com.mauricioliveira.awesomenotes.core.network.HttpClientFactory
import com.mauricioliveira.awesomenotes.core.network.NotesApi
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

/** Needs an AccessTokenProvider, which core:data provides. */
val networkModule = module {
    single {
        HttpClientFactory.create(
            engine = OkHttp.create(),
            baseUrl = BuildConfig.API_BASE_URL,
            tokenProvider = get(),
            enableLogging = BuildConfig.DEBUG,
        )
    }
    single { AuthApi(get()) }
    single { NotesApi(get()) }
}
