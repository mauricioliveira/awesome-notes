package com.mauricioliveira.awesomenotes.server.database

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.UUID

data class Note(
    val id: UUID,
    val userId: Long,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

class NoteRepository(private val database: Database) {

    suspend fun findAll(userId: Long): List<Note> = dbQuery(database) {
        NotesTable.selectAll()
            .where { NotesTable.userId eq userId }
            .orderBy(NotesTable.updatedAt, SortOrder.DESC)
            .map { it.toNote() }
    }

    /**
     * Inserts the note unless its id already exists.
     * Returns true when it was inserted, false when the id was already taken.
     */
    suspend fun insertIfAbsent(note: Note): Boolean = dbQuery(database) {
        NotesTable.insertIgnore {
            it[id] = note.id
            it[userId] = note.userId
            it[title] = note.title
            it[content] = note.content
            it[createdAt] = note.createdAt
            it[updatedAt] = note.updatedAt
        }.insertedCount == 1
    }

    /** Finds a note by id, whoever owns it. Used to tell a retry from an id conflict. */
    suspend fun findById(id: UUID): Note? = dbQuery(database) {
        NotesTable.selectAll()
            .where { NotesTable.id eq id }
            .singleOrNull()
            ?.toNote()
    }

    /** Returns the updated note, or null when the user has no note with this id. */
    suspend fun update(id: UUID, userId: Long, title: String, content: String, updatedAt: Instant): Note? =
        dbQuery(database) {
            val updated = NotesTable.update({ (NotesTable.id eq id) and (NotesTable.userId eq userId) }) {
                it[NotesTable.title] = title
                it[NotesTable.content] = content
                it[NotesTable.updatedAt] = updatedAt
            }
            if (updated == 0) {
                null
            } else {
                NotesTable.selectAll().where { NotesTable.id eq id }.single().toNote()
            }
        }

    /** Returns false when the user has no note with this id. */
    suspend fun delete(id: UUID, userId: Long): Boolean = dbQuery(database) {
        NotesTable.deleteWhere { (NotesTable.id eq id) and (NotesTable.userId eq userId) } > 0
    }

    private fun ResultRow.toNote() = Note(
        id = this[NotesTable.id],
        userId = this[NotesTable.userId],
        title = this[NotesTable.title],
        content = this[NotesTable.content],
        createdAt = this[NotesTable.createdAt],
        updatedAt = this[NotesTable.updatedAt],
    )
}
