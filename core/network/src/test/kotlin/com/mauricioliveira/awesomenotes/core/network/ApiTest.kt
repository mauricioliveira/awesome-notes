package com.mauricioliveira.awesomenotes.core.network

import com.mauricioliveira.awesomenotes.core.network.dto.CredentialsDto
import com.mauricioliveira.awesomenotes.core.network.dto.NewNoteDto
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ApiTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val requests = mutableListOf<HttpRequestData>()
    private var storedToken: String? = null

    private fun client(status: HttpStatusCode = HttpStatusCode.OK, body: String = "") =
        HttpClientFactory.create(
            engine = MockEngine { request ->
                requests += request
                respond(body, status, jsonHeaders)
            },
            baseUrl = "http://api.test/",
            tokenProvider = { storedToken },
        )

    @Test
    fun `login posts the credentials and parses the token`() = runTest {
        val api = AuthApi(client(body = """{"token":"abc"}"""))

        val response = api.login(CredentialsDto("maria", "s3cretpass"))

        assertEquals("abc", response.token)
        val request = requests.single()
        assertEquals("http://api.test/auth/login", request.url.toString())
        assertEquals("""{"username":"maria","password":"s3cretpass"}""", request.body.toByteArray().decodeToString())
    }

    @Test
    fun `requests carry the stored token, reloaded after clearCachedToken`() = runTest {
        val client = client(body = "[]")
        val notesApi = NotesApi(client)
        val authApi = AuthApi(client)

        notesApi.getNotes()
        storedToken = "first"
        authApi.clearCachedToken()
        notesApi.getNotes()
        storedToken = "second"
        notesApi.getNotes()
        authApi.clearCachedToken()
        notesApi.getNotes()

        val headers = requests.map { it.headers[HttpHeaders.Authorization] }
        assertEquals(listOf(null, "Bearer first", "Bearer first", "Bearer second"), headers)
    }

    @Test
    fun `createNote sends the client id and parses the note`() = runTest {
        val note = """{"id":"n1","title":"T","content":"C","createdAt":"2026-11-18T10:00:00Z","updatedAt":"2026-11-18T10:00:00Z"}"""
        val api = NotesApi(client(status = HttpStatusCode.Created, body = note))

        val created = api.createNote(NewNoteDto(id = "n1", title = "T", content = "C"))

        assertEquals("n1", created.id)
        assertEquals("""{"id":"n1","title":"T","content":"C"}""", requests.single().body.toByteArray().decodeToString())
    }

    @Test
    fun `an error status throws ClientRequestException`() = runTest {
        val api = NotesApi(client(status = HttpStatusCode.Unauthorized, body = """{"code":"UNAUTHORIZED","message":"x"}"""))

        assertFailsWith<ClientRequestException> { api.getNotes() }
        assertNull(requests.first().headers[HttpHeaders.Authorization])
    }
}
