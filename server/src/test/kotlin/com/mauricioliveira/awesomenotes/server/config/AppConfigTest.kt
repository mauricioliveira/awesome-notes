package com.mauricioliveira.awesomenotes.server.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppConfigTest {

    private val validEnv = mapOf(
        "DATABASE_URL" to "jdbc:postgresql://db:5432/awesomenotes",
        "DATABASE_USER" to "user",
        "DATABASE_PASSWORD" to "secret",
    )

    @Test
    fun `uses port 8080 when PORT is not set`() {
        val config = AppConfig.fromEnv(validEnv)

        assertEquals(8080, config.port)
    }

    @Test
    fun `reads PORT when set`() {
        val config = AppConfig.fromEnv(validEnv + ("PORT" to "9000"))

        assertEquals(9000, config.port)
    }

    @Test
    fun `fails when a database variable is missing`() {
        val error = assertFailsWith<IllegalStateException> {
            AppConfig.fromEnv(validEnv - "DATABASE_URL")
        }

        assertEquals("Missing required environment variable: DATABASE_URL", error.message)
    }
}