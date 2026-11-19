package com.mauricioliveira.awesomenotes.core.testing

import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.domain.model.Note
import com.mauricioliveira.awesomenotes.core.domain.repository.NoteRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

/** Behaves like the real repository: successful writes update the observed notes. */
class FakeNoteRepository(vararg initial: Note) : NoteRepository {

    val notes = MutableStateFlow(initial.toList())

    /** The error the next calls fail with, or null for success. */
    var error: AppError? = null

    /** When set, every suspend call waits for it, so a test can look at the state mid-request. */
    var gate: CompletableDeferred<Unit>? = null

    var refreshCalls = 0
        private set
    val createdIds = mutableListOf<String>()

    override fun observeNotes(): Flow<List<Note>> =
        notes.map { all -> all.sortedByDescending { it.updatedAt } }

    override fun observeNote(id: String): Flow<Note?> =
        notes.map { all -> all.find { it.id == id } }

    override suspend fun refresh(): Result<Unit> {
        refreshCalls++
        return respond { }
    }

    override suspend fun create(id: String, title: String, content: String): Result<Note> {
        createdIds += id
        return respond {
            val note = Note(id, title, content, NOW, NOW)
            notes.update { it.filterNot { n -> n.id == id } + note }
            note
        }
    }

    override suspend fun update(id: String, title: String, content: String): Result<Note> = respond {
        val existing = notes.value.first { it.id == id }
        val note = existing.copy(title = title, content = content, updatedAt = NOW)
        notes.update { all -> all.map { if (it.id == id) note else it } }
        note
    }

    override suspend fun delete(id: String): Result<Unit> = respond {
        notes.update { all -> all.filterNot { it.id == id } }
    }

    private suspend fun <T> respond(onSuccess: () -> T): Result<T> {
        gate?.await()
        error?.let { return Result.failure(it) }
        return Result.success(onSuccess())
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-19T12:00:00Z")
    }
}
