package com.mauricioliveira.awesomenotes.feature.notes.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mauricioliveira.awesomenotes.feature.notes.editor.NoteEditorRoute
import com.mauricioliveira.awesomenotes.feature.notes.list.NotesListRoute
import kotlinx.serialization.Serializable

@Serializable
data object NotesListDestination

/** [noteId] null opens the editor for a new note. */
@Serializable
data class NoteEditorDestination(val noteId: String? = null)

fun NavController.navigateToEditor(noteId: String? = null) = navigate(NoteEditorDestination(noteId))

fun NavGraphBuilder.notesListScreen(onOpenNote: (String) -> Unit, onNewNote: () -> Unit) {
    composable<NotesListDestination> {
        NotesListRoute(onOpenNote = onOpenNote, onNewNote = dropUnlessResumed { onNewNote() })
    }
}

fun NavGraphBuilder.noteEditorScreen(onClose: () -> Unit) {
    composable<NoteEditorDestination> { entry ->
        NoteEditorRoute(
            noteId = entry.toRoute<NoteEditorDestination>().noteId,
            onClose = dropUnlessResumed { onClose() },
        )
    }
}
