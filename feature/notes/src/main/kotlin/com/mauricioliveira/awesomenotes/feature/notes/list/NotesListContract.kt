package com.mauricioliveira.awesomenotes.feature.notes.list

import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.domain.model.Note

sealed interface NotesListState {

    /** Nothing cached yet and the first refresh is running. */
    data object Loading : NotesListState

    /** Nothing cached and the refresh failed: full screen error with "Try again". */
    data class Error(val error: AppError) : NotesListState

    /** The server has no notes for this user. */
    data class Empty(val isRefreshing: Boolean) : NotesListState

    /** Cached notes. A failed refresh keeps them on screen and shows [refreshError] in a snackbar. */
    data class Content(
        val notes: List<Note>,
        val isRefreshing: Boolean,
        val refreshError: AppError?,
    ) : NotesListState
}

sealed interface NotesListIntent {
    data object Refresh : NotesListIntent
    data object Logout : NotesListIntent
}
