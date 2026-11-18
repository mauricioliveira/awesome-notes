package com.mauricioliveira.awesomenotes.server.docs

import io.ktor.server.routing.routing
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
        application { routing { docsRoutes() } }

        val response = client.get("/openapi/openapi.yaml")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("title: Awesome Notes API"))
    }

    @Test
    fun `serves SwaggerUI`() = testApplication {
        application { routing { docsRoutes() } }

        val response = client.get("/docs")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}