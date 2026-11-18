package com.mauricioliveira.awesomenotes.server.auth

import com.mauricioliveira.awesomenotes.server.support.jsonField
import com.mauricioliveira.awesomenotes.server.support.postJson
import com.mauricioliveira.awesomenotes.server.support.registerAndLogin
import com.mauricioliveira.awesomenotes.server.support.serverTest
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest {

    private val maria = """{"username":"maria","password":"s3cretpass"}"""

    @Test
    fun `register returns 201`() = serverTest {
        val response = client.postJson("/auth/register", maria)

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `register with a taken username returns 409`() = serverTest {
        client.postJson("/auth/register", maria)

        val response = client.postJson("/auth/register", maria)

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals("USERNAME_TAKEN", response.jsonField("code"))
    }

    @Test
    fun `register with a short password returns 400`() = serverTest {
        val response = client.postJson("/auth/register", """{"username":"maria","password":"short"}""")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("VALIDATION_ERROR", response.jsonField("code"))
    }

    @Test
    fun `login returns a token`() = serverTest {
        client.postJson("/auth/register", maria)

        val response = client.postJson("/auth/login", maria)

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.jsonField("token").isNotBlank())
    }

    @Test
    fun `login with a wrong password returns 401`() = serverTest {
        client.postJson("/auth/register", maria)

        val response = client.postJson("/auth/login", """{"username":"maria","password":"wrongpass"}""")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("INVALID_CREDENTIALS", response.jsonField("code"))
    }

    @Test
    fun `logout revokes the token`() = serverTest {
        val token = client.registerAndLogin()

        val logout = client.post("/auth/logout") { bearerAuth(token) }
        val secondLogout = client.post("/auth/logout") { bearerAuth(token) }

        assertEquals(HttpStatusCode.NoContent, logout.status)
        assertEquals(HttpStatusCode.Unauthorized, secondLogout.status)
    }

    @Test
    fun `logout without a token returns 401`() = serverTest {
        val response = client.post("/auth/logout")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("UNAUTHORIZED", response.jsonField("code"))
    }
}
