package com.mauricioliveira.awesomenotes.core.data.fake

import com.mauricioliveira.awesomenotes.core.database.dao.NoteDao
import com.mauricioliveira.awesomenotes.core.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory NoteDao. replaceAll keeps the interface's default body (deleteAll + upsertAll). */
class FakeNoteDao(vararg initial: NoteEntity) : NoteDao {

    val notes = MutableStateFlow(initial.associateBy { it.id })

    override fun observeAll(): Flow<List<NoteEntity>> =
        notes.map { all -> all.values.sortedByDescending { it.updatedAtMillis } }

    override fun observeById(id: String): Flow<NoteEntity?> = notes.map { it[id] }

    override suspend fun upsert(note: NoteEntity) = notes.update { it + (note.id to note) }

    override suspend fun upsertAll(notes: List<NoteEntity>) =
        this.notes.update { current -> current + notes.associateBy { it.id } }

    override suspend fun deleteById(id: String) = notes.update { it - id }

    override suspend fun deleteAll() {
        notes.value = emptyMap()
    }
}
