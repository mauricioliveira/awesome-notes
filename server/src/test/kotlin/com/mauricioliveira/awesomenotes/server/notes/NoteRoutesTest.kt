package com.mauricioliveira.awesomenotes.server.notes

import com.mauricioliveira.awesomenotes.server.support.jsonField
import com.mauricioliveira.awesomenotes.server.support.postJson
import com.mauricioliveira.awesomenotes.server.support.registerAndLogin
import com.mauricioliveira.awesomenotes.server.support.serverTest
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class NoteRoutesTest {

    private val noteId = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    private val newNote = """{"id":"$noteId","title":"Shopping list","content":"Milk"}"""

    @Test
    fun `created note appears in the list`() = serverTest {
        val token = client.registerAndLogin()

        val create = client.postJson("/notes", newNote, token)
        val list = client.get("/notes") { bearerAuth(token) }

        assertEquals(HttpStatusCode.Created, create.status)
        val notes = Json.parseToJsonElement(list.bodyAsText()).jsonArray
        assertEquals(1, notes.size)
        assertEquals("Shopping list", notes[0].jsonObject.getValue("title").jsonPrimitive.content)
    }

    @Test
    fun `retrying a create returns 200 and keeps the note`() = serverTest {
        val token = client.registerAndLogin()
        client.postJson("/notes", newNote, token)

        val retry = client.postJson("/notes", """{"id":"$noteId","title":"Changed","content":""}""", token)

        assertEquals(HttpStatusCode.OK, retry.status)
        assertEquals("Shopping list", retry.jsonField("title"))
    }

    @Test
    fun `creating with another user's note id returns 409`() = serverTest {
        val maria = client.registerAndLogin("maria")
        val joao = client.registerAndLogin("joao")
        client.postJson("/notes", newNote, maria)

        val response = client.postJson("/notes", newNote, joao)

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals("NOTE_ID_CONFLICT", response.jsonField("code"))
    }

    @Test
    fun `another user cannot see, update or delete the note`() = serverTest {
        val maria = client.registerAndLogin("maria")
        val joao = client.registerAndLogin("joao")
        client.postJson("/notes", newNote, maria)

        val list = client.get("/notes") { bearerAuth(joao) }
        val update = client.put("/notes/$noteId") {
            bearerAuth(joao)
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Hacked","content":""}""")
        }
        val delete = client.delete("/notes/$noteId") { bearerAuth(joao) }

        assertEquals("[]", list.bodyAsText())
        assertEquals(HttpStatusCode.NotFound, update.status)
        assertEquals(HttpStatusCode.NotFound, delete.status)
    }

    @Test
    fun `owner can update and delete the note`() = serverTest {
        val token = client.registerAndLogin()
        client.postJson("/notes", newNote, token)

        val update = client.put("/notes/$noteId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Groceries","content":"Milk, eggs"}""")
        }
        val delete = client.delete("/notes/$noteId") { bearerAuth(token) }
        val deleteAgain = client.delete("/notes/$noteId") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, update.status)
        assertEquals("Groceries", update.jsonField("title"))
        assertEquals(HttpStatusCode.NoContent, delete.status)
        assertEquals(HttpStatusCode.NotFound, deleteAgain.status)
    }

    @Test
    fun `a revoked token cannot access notes`() = serverTest {
        val token = client.registerAndLogin()
        client.post("/auth/logout") { bearerAuth(token) }

        val response = client.get("/notes") { bearerAuth(token) }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("UNAUTHORIZED", response.jsonField("code"))
    }

    @Test
    fun `blank title returns 400`() = serverTest {
        val token = client.registerAndLogin()

        val response = client.postJson("/notes", """{"id":"$noteId","title":"  ","content":""}""", token)

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("VALIDATION_ERROR", response.jsonField("code"))
    }
}
