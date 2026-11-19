package com.mauricioliveira.awesomenotes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mauricioliveira.awesomenotes.feature.auth.navigation.LoginDestination
import com.mauricioliveira.awesomenotes.feature.auth.navigation.loginScreen
import com.mauricioliveira.awesomenotes.feature.auth.navigation.navigateToRegister
import com.mauricioliveira.awesomenotes.feature.auth.navigation.registerScreen

@Composable
fun AuthNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = LoginDestination) {
        loginScreen(onCreateAccount = navController::navigateToRegister)
        registerScreen(onBack = navController::navigateUp)
    }
}
