package com.mauricioliveira.awesomenotes.server.database

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

class SessionRepository(private val database: Database) {

    suspend fun create(token: String, userId: Long) {
        dbQuery(database) {
            SessionsTable.insert {
                it[SessionsTable.token] = token
                it[SessionsTable.userId] = userId
            }
        }
    }

    suspend fun findUserId(token: String): Long? = dbQuery(database) {
        SessionsTable.selectAll()
            .where { SessionsTable.token eq token }
            .singleOrNull()
            ?.get(SessionsTable.userId)
    }

    /** Returns false when there was no session with this token. */
    suspend fun delete(token: String): Boolean = dbQuery(database) {
        SessionsTable.deleteWhere { SessionsTable.token eq token } > 0
    }
}
