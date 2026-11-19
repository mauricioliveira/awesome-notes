package com.mauricioliveira.awesomenotes.feature.auth.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LoginRoute(
    onCreateAccount: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LoginScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onCreateAccount = onCreateAccount,
    )
}
