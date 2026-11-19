package com.mauricioliveira.awesomenotes.feature.notes.list

import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.testing.FakeAuthRepository
import com.mauricioliveira.awesomenotes.core.testing.FakeNoteRepository
import com.mauricioliveira.awesomenotes.core.testing.MainDispatcherRule
import com.mauricioliveira.awesomenotes.core.testing.aNote
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class NotesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noteRepository = FakeNoteRepository()
    private val authRepository = FakeAuthRepository()

    private fun viewModel() = NotesListViewModel(noteRepository, authRepository)

    /** First state that is not waiting on a refresh. */
    private suspend fun NotesListViewModel.settledState() = state.first {
        when (it) {
            NotesListState.Loading -> false
            is NotesListState.Error -> true
            is NotesListState.Empty -> !it.isRefreshing
            is NotesListState.Content -> !it.isRefreshing
        }
    }

    @Test
    fun `shows the cached notes, newest first, and refreshes on open`() = runTest {
        val older = aNote(id = "a", updatedAt = java.time.Instant.parse("2026-11-18T10:00:00Z"))
        val newer = aNote(id = "b", updatedAt = java.time.Instant.parse("2026-11-19T10:00:00Z"))
        noteRepository.notes.value = listOf(older, newer)

        val state = viewModel().settledState()

        assertEquals(NotesListState.Content(listOf(newer, older), isRefreshing = false, refreshError = null), state)
        assertEquals(1, noteRepository.refreshCalls)
    }

    @Test
    fun `nothing cached shows loading until the first refresh ends`() = runTest {
        val gate = CompletableDeferred<Unit>()
        noteRepository.gate = gate
        val viewModel = viewModel()

        assertEquals(NotesListState.Loading, viewModel.state.first())

        gate.complete(Unit)
        assertEquals(NotesListState.Empty(isRefreshing = false), viewModel.settledState())
    }

    @Test
    fun `a failed refresh with nothing cached shows the full screen error`() = runTest {
        noteRepository.error = AppError.NoConnection

        assertEquals(NotesListState.Error(AppError.NoConnection), viewModel().settledState())
    }

    @Test
    fun `a failed refresh keeps the cached notes and exposes the error`() = runTest {
        noteRepository.notes.value = listOf(aNote())
        noteRepository.error = AppError.NoConnection

        assertEquals(
            NotesListState.Content(listOf(aNote()), isRefreshing = false, refreshError = AppError.NoConnection),
            viewModel().settledState(),
        )
    }

    @Test
    fun `refresh after an error clears it`() = runTest {
        noteRepository.notes.value = listOf(aNote())
        noteRepository.error = AppError.NoConnection
        val viewModel = viewModel()
        viewModel.settledState()

        noteRepository.error = null
        viewModel.onIntent(NotesListIntent.Refresh)

        assertEquals(
            NotesListState.Content(listOf(aNote()), isRefreshing = false, refreshError = null),
            viewModel.settledState(),
        )
        assertEquals(2, noteRepository.refreshCalls)
    }

    @Test
    fun `a refresh while one is running is ignored`() = runTest {
        val gate = CompletableDeferred<Unit>()
        noteRepository.gate = gate
        val viewModel = viewModel()

        viewModel.onIntent(NotesListIntent.Refresh)

        assertEquals(1, noteRepository.refreshCalls)
        gate.complete(Unit)
    }

    @Test
    fun `logout ends the session`() = runTest {
        authRepository.login("maria", "s3cretpass")
        val viewModel = viewModel()

        viewModel.onIntent(NotesListIntent.Logout)

        assertEquals(1, authRepository.logoutCalls)
        assertEquals(false, authRepository.isLoggedIn.value)
    }
}
