package com.mauricioliveira.awesomenotes.feature.notes.editor

import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import java.time.Instant

/** Which screen the editor shows. Only [Editing] carries data. */
sealed interface NoteEditorState {

    /** Reading the note from the cache. */
    data object Loading : NoteEditorState

    /** Not in the cache, or deleted on another device. */
    data object NotFound : NoteEditorState

    /** Saved, deleted or discarded. The route navigates back. */
    data object Closed : NoteEditorState

    data class Editing(
        val isNew: Boolean,
        val title: String,
        val content: String,
        /** The text as the server has it. The edits are compared with it. */
        val savedTitle: String,
        val savedContent: String,
        /** Null for a note that was never saved. */
        val updatedAt: Instant?,
        /** A save was tried with a blank title. Cleared when the title changes. */
        val titleMissing: Boolean = false,
        /** The request running now, if any. */
        val inProgress: EditorAction? = null,
        /** The last request that failed. The snackbar offers to retry it. */
        val failed: EditorFailure? = null,
        val dialog: EditorDialog? = null,
    ) : NoteEditorState {

        val hasChanges: Boolean
            get() = title != savedTitle || content != savedContent

        val canSave: Boolean
            get() = hasChanges && inProgress == null

        val canDelete: Boolean
            get() = !isNew && inProgress == null
    }
}

/** The two writes, both need the server (ADR-014). */
enum class EditorAction { Save, Delete }

data class EditorFailure(val action: EditorAction, val error: AppError)

enum class EditorDialog { Discard, Delete }

sealed interface NoteEditorIntent {
    data class TitleChanged(val value: String) : NoteEditorIntent
    data class ContentChanged(val value: String) : NoteEditorIntent
    data object Save : NoteEditorIntent

    /** Delete button: asks first. */
    data object Delete : NoteEditorIntent
    data object ConfirmDelete : NoteEditorIntent

    /** Back button or gesture: asks first when there are unsaved changes. */
    data object Back : NoteEditorIntent
    data object ConfirmDiscard : NoteEditorIntent

    data object DismissDialog : NoteEditorIntent
    data object Retry : NoteEditorIntent
}
