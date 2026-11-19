package com.mauricioliveira.awesomenotes.di

import com.mauricioliveira.awesomenotes.MainViewModel
import com.mauricioliveira.awesomenotes.core.data.di.dataModule
import com.mauricioliveira.awesomenotes.core.database.di.databaseModule
import com.mauricioliveira.awesomenotes.core.network.di.networkModule
import com.mauricioliveira.awesomenotes.core.storage.di.storageModule
import com.mauricioliveira.awesomenotes.feature.auth.di.authModule
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private val mainModule = module {
    viewModel { MainViewModel(authRepository = get()) }
}

/** Every Koin module of the app. The app is the only place that knows them all (ADR-018). */
val appModules = listOf(
    networkModule,
    storageModule,
    databaseModule,
    dataModule,
    authModule,
    mainModule,
)
