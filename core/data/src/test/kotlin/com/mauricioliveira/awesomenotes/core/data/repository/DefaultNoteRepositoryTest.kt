package com.mauricioliveira.awesomenotes.core.data.repository

import com.mauricioliveira.awesomenotes.core.data.fake.FakeNoteDao
import com.mauricioliveira.awesomenotes.core.data.fake.FakeServer
import com.mauricioliveira.awesomenotes.core.data.fake.FakeTokenStore
import com.mauricioliveira.awesomenotes.core.data.fake.noteJson
import com.mauricioliveira.awesomenotes.core.data.fake.reply
import com.mauricioliveira.awesomenotes.core.data.fake.replyError
import com.mauricioliveira.awesomenotes.core.data.session.LocalSession
import com.mauricioliveira.awesomenotes.core.database.entity.NoteEntity
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultNoteRepositoryTest {

    private val stale = NoteEntity("stale", "Stale", "C", 0, 0)
    private val tokenStore = FakeTokenStore(initial = "abc")
    private val noteDao = FakeNoteDao(stale)
    private val server = FakeServer(tokenStore)
    private val repository = DefaultNoteRepository(
        notesApi = server.notesApi,
        noteDao = noteDao,
        session = LocalSession(tokenStore, noteDao, server.authApi),
    )

    @Test
    fun `refresh replaces the cache with the server notes, newest first`() = runTest {
        server.handler = {
            reply(HttpStatusCode.OK, "[${noteJson("a", updatedAt = "2026-11-18T10:00:00Z")},${noteJson("b", updatedAt = "2026-11-18T11:00:00Z")}]")
        }

        val result = repository.refresh()

        assertTrue(result.isSuccess)
        assertEquals(listOf("b", "a"), repository.observeNotes().first().map { it.id })
    }

    @Test
    fun `failed refresh keeps the cached notes`() = runTest {
        server.handler = { throw IOException("offline") }

        val result = repository.refresh()

        assertEquals(AppError.NoConnection, result.exceptionOrNull())
        assertEquals(listOf("stale"), repository.observeNotes().first().map { it.id })
    }

    @Test
    fun `create caches the note the server returns`() = runTest {
        server.handler = { reply(HttpStatusCode.Created, noteJson("n1", title = "Hello")) }

        val note = repository.create("n1", "Hello", "C").getOrThrow()

        assertEquals(Instant.parse("2026-11-18T09:00:00Z"), note.createdAt)
        assertEquals(note, repository.observeNote("n1").first())
    }

    @Test
    fun `a revoked token ends the local session`() = runTest {
        server.handler = { replyError(HttpStatusCode.Unauthorized, "UNAUTHORIZED") }

        val result = repository.refresh()

        assertEquals(AppError.Unauthorized, result.exceptionOrNull())
        assertNull(tokenStore.token.value)
        assertTrue(noteDao.notes.value.isEmpty())
    }

    @Test
    fun `updating a note deleted elsewhere returns NotFound and drops the stale copy`() = runTest {
        server.handler = { replyError(HttpStatusCode.NotFound, "NOT_FOUND") }

        val result = repository.update("stale", "New", "C")

        assertEquals(AppError.NotFound, result.exceptionOrNull())
        assertNull(repository.observeNote("stale").first())
    }

    @Test
    fun `deleting a note already gone on the server counts as success`() = runTest {
        server.handler = { replyError(HttpStatusCode.NotFound, "NOT_FOUND") }

        val result = repository.delete("stale")

        assertTrue(result.isSuccess)
        assertNull(repository.observeNote("stale").first())
    }

    @Test
    fun `a failed delete keeps the note`() = runTest {
        server.handler = { reply(HttpStatusCode.InternalServerError) }

        val result = repository.delete("stale")

        assertEquals(AppError.Server(500), result.exceptionOrNull())
        assertEquals(listOf("stale"), repository.observeNotes().first().map { it.id })
    }
}
