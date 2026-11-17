package com.mauricioliveira.awesomenotes.server.docs

import com.mauricioliveira.awesomenotes.server.module
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

class DocsRoutesTests {

    @Test
    fun `serves the OpenApi contract`() = testApplication {
        application { module() }

        val response = client.get("/openapi/openapi.yaml")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("title: Awesome Notes API"))
    }

    @Test
    fun `serves SwaggerUI`() = testApplication {
        application { module() }

        val response = client.get("/docs")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}