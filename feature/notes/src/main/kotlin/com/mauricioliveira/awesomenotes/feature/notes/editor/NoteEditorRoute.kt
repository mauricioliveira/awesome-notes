package com.mauricioliveira.awesomenotes.feature.notes.editor

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun NoteEditorRoute(
    noteId: String?,
    onClose: () -> Unit,
    viewModel: NoteEditorViewModel = koinViewModel { parametersOf(noteId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val closed = state == NoteEditorState.Closed

    // Closed is a state, not an event, so it survives rotation and is never handled twice.
    LaunchedEffect(closed) {
        if (closed) onClose()
    }
    // The system back gesture follows the same rule as the back button (unsaved changes ask first).
    BackHandler(enabled = !closed) {
        viewModel.onIntent(NoteEditorIntent.Back)
    }

    NoteEditorScreen(state = state, onIntent = viewModel::onIntent, onClose = onClose)
}
