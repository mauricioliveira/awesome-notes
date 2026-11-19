package com.mauricioliveira.awesomenotes.feature.notes.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun NotesListRoute(
    onOpenNote: (String) -> Unit,
    onNewNote: () -> Unit,
    viewModel: NotesListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()

    NotesListScreen(
        state = state,
        username = username,
        onIntent = viewModel::onIntent,
        onOpenNote = onOpenNote,
        onNewNote = onNewNote,
    )
}
