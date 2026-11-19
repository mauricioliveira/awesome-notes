package com.mauricioliveira.awesomenotes.feature.auth.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mauricioliveira.awesomenotes.feature.auth.login.LoginRoute
import com.mauricioliveira.awesomenotes.feature.auth.register.RegisterRoute
import kotlinx.serialization.Serializable

@Serializable
data object LoginDestination

@Serializable
data object RegisterDestination

fun NavController.navigateToRegister() = navigate(RegisterDestination)

fun NavGraphBuilder.loginScreen(onCreateAccount: () -> Unit) {
    composable<LoginDestination> {
        LoginRoute(onCreateAccount = dropUnlessResumed { onCreateAccount() })
    }
}

fun NavGraphBuilder.registerScreen(onBack: () -> Unit) {
    composable<RegisterDestination> {
        RegisterRoute(onBack = dropUnlessResumed { onBack() })
    }
}
