package com.mauricioliveira.awesomenotes.server.health

import com.mauricioliveira.awesomenotes.server.module
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.Test
import kotlin.test.assertEquals

class HealthRoutesTest {

    @Test
    fun `health returns 200 OK`() = testApplication {
        application { module() }

        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.bodyAsText())
    }
}