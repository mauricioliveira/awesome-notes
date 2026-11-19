package com.mauricioliveira.awesomenotes.feature.auth.di

import com.mauricioliveira.awesomenotes.feature.auth.login.LoginViewModel
import com.mauricioliveira.awesomenotes.feature.auth.register.RegisterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    viewModel { LoginViewModel(savedStateHandle = get(), authRepository = get()) }
    viewModel { RegisterViewModel(savedStateHandle = get(), authRepository = get()) }
}
