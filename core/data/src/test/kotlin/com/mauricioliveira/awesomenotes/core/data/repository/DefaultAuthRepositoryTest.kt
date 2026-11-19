package com.mauricioliveira.awesomenotes.core.data.repository

import com.mauricioliveira.awesomenotes.core.data.fake.FakeNoteDao
import com.mauricioliveira.awesomenotes.core.data.fake.FakeServer
import com.mauricioliveira.awesomenotes.core.data.fake.FakeTokenStore
import com.mauricioliveira.awesomenotes.core.data.fake.reply
import com.mauricioliveira.awesomenotes.core.data.fake.replyError
import com.mauricioliveira.awesomenotes.core.data.session.LocalSession
import com.mauricioliveira.awesomenotes.core.database.entity.NoteEntity
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultAuthRepositoryTest {

    private val tokenStore = FakeTokenStore()
    private val noteDao = FakeNoteDao(NoteEntity("old", "Old", "C", 0, 0))
    private val server = FakeServer(tokenStore)
    private val repository = DefaultAuthRepository(
        authApi = server.authApi,
        tokenStore = tokenStore,
        session = LocalSession(tokenStore, noteDao, server.authApi),
    )

    @Test
    fun `login stores the token, drops cached notes and sends the token next time`() = runTest {
        server.handler = { reply(HttpStatusCode.OK, """{"token":"abc"}""") }

        val result = repository.login("maria", "s3cretpass")

        assertTrue(result.isSuccess)
        assertEquals("abc", tokenStore.token.value)
        assertEquals("maria", repository.username.first())
        assertTrue(repository.isLoggedIn.first())
        assertTrue(noteDao.notes.value.isEmpty())

        server.handler = { reply(HttpStatusCode.OK, "[]") }
        server.notesApi.getNotes()
        assertEquals("Bearer abc", server.requests.last().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `wrong password maps to InvalidCredentials and stores nothing`() = runTest {
        server.handler = { replyError(HttpStatusCode.Unauthorized, "INVALID_CREDENTIALS") }

        val result = repository.login("maria", "wrong")

        assertEquals(AppError.InvalidCredentials, result.exceptionOrNull())
        assertNull(tokenStore.token.value)
    }

    @Test
    fun `taken username maps to UsernameTaken`() = runTest {
        server.handler = { replyError(HttpStatusCode.Conflict, "USERNAME_TAKEN") }

        val result = repository.register("maria", "s3cretpass")

        assertEquals(AppError.UsernameTaken, result.exceptionOrNull())
    }

    @Test
    fun `no network maps to NoConnection`() = runTest {
        server.handler = { throw IOException("offline") }

        val result = repository.login("maria", "s3cretpass")

        assertEquals(AppError.NoConnection, result.exceptionOrNull())
    }

    @Test
    fun `logout clears the token and the notes even when the server is unreachable`() = runTest {
        tokenStore.save("abc", "maria")
        server.handler = { throw IOException("offline") }

        repository.logout()

        assertNull(tokenStore.token.value)
        assertNull(repository.username.first())
        assertTrue(noteDao.notes.value.isEmpty())
        assertEquals(false, repository.isLoggedIn.first())
    }
}
