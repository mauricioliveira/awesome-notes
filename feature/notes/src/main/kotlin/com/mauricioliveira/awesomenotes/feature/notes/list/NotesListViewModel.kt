package com.mauricioliveira.awesomenotes.feature.notes.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.domain.model.Note
import com.mauricioliveira.awesomenotes.core.domain.model.asAppError
import com.mauricioliveira.awesomenotes.core.domain.repository.AuthRepository
import com.mauricioliveira.awesomenotes.core.domain.repository.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesListViewModel(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    /** Outcome of the network refreshes. The notes themselves always come from Room (ADR-014). */
    private data class RefreshStatus(
        val isRefreshing: Boolean = true,
        val error: AppError? = null,
        /** False until a refresh succeeds, so an empty cache shows Loading, not Empty, on first open. */
        val hasSucceeded: Boolean = false,
    )

    private val refreshStatus = MutableStateFlow(RefreshStatus())
    private var refreshJob: Job? = null

    val state: StateFlow<NotesListState> =
        combine(noteRepository.observeNotes(), refreshStatus, ::toState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NotesListState.Loading,
            )

    /** For the avatar and the account menu. */
    val username: StateFlow<String?> =
        authRepository.username.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        refresh()
    }

    fun onIntent(intent: NotesListIntent) {
        when (intent) {
            NotesListIntent.Refresh -> refresh()
            NotesListIntent.Logout -> viewModelScope.launch { authRepository.logout() }
        }
    }

    private fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refreshStatus.update { it.copy(isRefreshing = true, error = null) }
            val failure = noteRepository.refresh().exceptionOrNull()
            refreshStatus.update {
                it.copy(
                    isRefreshing = false,
                    error = failure?.asAppError(),
                    hasSucceeded = it.hasSucceeded || failure == null,
                )
            }
        }
    }

    private fun toState(notes: List<Note>, status: RefreshStatus): NotesListState = when {
        notes.isNotEmpty() -> NotesListState.Content(notes, status.isRefreshing, status.error)
        status.error != null -> NotesListState.Error(status.error)
        status.hasSucceeded -> NotesListState.Empty(status.isRefreshing)
        else -> NotesListState.Loading
    }
}
