package com.mauricioliveira.awesomenotes.server.support

import com.mauricioliveira.awesomenotes.server.config.DatabaseConfig
import com.mauricioliveira.awesomenotes.server.database.DatabaseFactory
import com.mauricioliveira.awesomenotes.server.database.SessionsTable
import com.mauricioliveira.awesomenotes.server.database.UsersTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.postgresql.PostgreSQLContainer

object TestDatabase {

    private val container = PostgreSQLContainer("postgres:17-alpine").apply { start() }

    val database: Database = DatabaseFactory.connect(
        DatabaseConfig(
            url = container.jdbcUrl,
            user = container.username,
            password = container.password,
        ),
    )

    fun reset() {
        transaction(database) {
            SessionsTable.deleteAll()
            UsersTable.deleteAll()
        }
    }
}
