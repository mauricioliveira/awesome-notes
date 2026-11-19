package com.mauricioliveira.awesomenotes.core.data.repository

import com.mauricioliveira.awesomenotes.core.data.mapper.resultOf
import com.mauricioliveira.awesomenotes.core.data.mapper.toDomain
import com.mauricioliveira.awesomenotes.core.data.mapper.toEntity
import com.mauricioliveira.awesomenotes.core.data.session.LocalSession
import com.mauricioliveira.awesomenotes.core.database.dao.NoteDao
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.domain.model.Note
import com.mauricioliveira.awesomenotes.core.domain.repository.NoteRepository
import com.mauricioliveira.awesomenotes.core.network.NotesApi
import com.mauricioliveira.awesomenotes.core.network.dto.NewNoteDto
import com.mauricioliveira.awesomenotes.core.network.dto.NoteInputDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Reads come from Room. Writes go to the server first, then to Room (ADR-014). */
internal class DefaultNoteRepository(
    private val notesApi: NotesApi,
    private val noteDao: NoteDao,
    private val session: LocalSession,
) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> =
        noteDao.observeAll().map { notes -> notes.map { it.toDomain() } }

    override fun observeNote(id: String): Flow<Note?> =
        noteDao.observeById(id)
            .map { it?.toDomain() }
            .distinctUntilChanged()

    override suspend fun refresh(): Result<Unit> =
        call { noteDao.replaceAll(notesApi.getNotes().map { it.toEntity() }) }

    override suspend fun create(id: String, title: String, content: String): Result<Note> =
        call {
            val entity = notesApi.createNote(NewNoteDto(id, title, content)).toEntity()
            noteDao.upsert(entity)
            entity.toDomain()
        }

    override suspend fun update(id: String, title: String, content: String): Result<Note> =
        call {
            val entity = notesApi.updateNote(id, NoteInputDto(title, content)).toEntity()
            noteDao.upsert(entity)
            entity.toDomain()
        }.onFailure { error ->
            // Deleted on another device: drop the stale copy so the list stops showing it.
            if (error == AppError.NotFound) noteDao.deleteById(id)
        }

    override suspend fun delete(id: String): Result<Unit> {
        val result = call { notesApi.deleteNote(id) }
        // Already deleted on the server: the user's goal is reached, so it counts as success.
        if (result.isFailure && result.exceptionOrNull() != AppError.NotFound) return result
        noteDao.deleteById(id)
        return Result.success(Unit)
    }

    /** Like [resultOf], and ends the local session when the server no longer accepts the token. */
    private suspend inline fun <T> call(block: () -> T): Result<T> =
        resultOf(block).onFailure { error ->
            if (error == AppError.Unauthorized) session.clear()
        }
}
