package com.mauricioliveira.awesomenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mauricioliveira.awesomenotes.core.ui.component.LoadingView
import com.mauricioliveira.awesomenotes.core.ui.theme.AwesomeNotesTheme
import com.mauricioliveira.awesomenotes.navigation.AuthNavHost
import com.mauricioliveira.awesomenotes.navigation.NotesPlaceholder
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AwesomeNotesTheme {
                val viewModel: MainViewModel = koinViewModel()
                val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

                // The session decides which graph exists, so login, logout and an expired
                // session never leave screens of the other graph in the back stack (ADR-023).
                when (sessionState) {
                    SessionState.Loading -> LoadingView()
                    SessionState.LoggedOut -> AuthNavHost()
                    SessionState.LoggedIn -> NotesPlaceholder(onLogout = viewModel::logout)
                }
            }
        }
    }
}
