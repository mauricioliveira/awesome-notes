package com.mauricioliveira.awesomenotes.server.notes

import com.mauricioliveira.awesomenotes.server.database.Note
import com.mauricioliveira.awesomenotes.server.database.NoteRepository
import com.mauricioliveira.awesomenotes.server.error.NoteIdConflictException
import com.mauricioliveira.awesomenotes.server.error.NoteNotFoundException
import com.mauricioliveira.awesomenotes.server.error.ValidationException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

data class CreateNoteResult(val note: Note, val created: Boolean)

class NoteService(
    private val notes: NoteRepository,
    private val clock: () -> Instant = { Instant.now() },
) {

    suspend fun list(userId: Long): List<Note> = notes.findAll(userId)

    /** created is false when the same user retries with an id that is already stored. */
    suspend fun create(userId: Long, id: UUID, title: String, content: String): CreateNoteResult {
        validate(title, content)
        val now = now()
        val note = Note(id, userId, title, content, createdAt = now, updatedAt = now)
        if (notes.insertIfAbsent(note)) return CreateNoteResult(note, created = true)

        val existing = notes.findById(id)
        if (existing == null || existing.userId != userId) throw NoteIdConflictException()
        return CreateNoteResult(existing, created = false)
    }

    suspend fun update(userId: Long, id: UUID, title: String, content: String): Note {
        validate(title, content)
        return notes.update(id, userId, title, content, updatedAt = now())
            ?: throw NoteNotFoundException()
    }

    suspend fun delete(userId: Long, id: UUID) {
        if (!notes.delete(id, userId)) throw NoteNotFoundException()
    }

    // PostgreSQL keeps microseconds; truncating keeps the response equal to what is stored.
    private fun now(): Instant = clock().truncatedTo(ChronoUnit.MILLIS)

    private fun validate(title: String, content: String) {
        if (title.isBlank() || title.length > TITLE_MAX_LENGTH) {
            throw ValidationException("Title must be between 1 and 255 characters.")
        }
        if (content.length > CONTENT_MAX_LENGTH) {
            throw ValidationException("Content must be at most 10000 characters.")
        }
    }

    private companion object {
        const val TITLE_MAX_LENGTH = 255
        const val CONTENT_MAX_LENGTH = 10_000
    }
}
