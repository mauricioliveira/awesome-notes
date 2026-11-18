package com.mauricioliveira.awesomenotes.server.database

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll

data class User(val id: Long, val username: String, val passwordHash: String)

class UserRepository(private val database: Database) {

    suspend fun findByUsername(username: String): User? = dbQuery(database) {
        UsersTable.selectAll()
            .where { UsersTable.username eq username }
            .singleOrNull()
            ?.toUser()
    }

    /** Returns false when the username is already taken. */
    suspend fun create(username: String, passwordHash: String): Boolean = dbQuery(database) {
        UsersTable.insertIgnore {
            it[UsersTable.username] = username
            it[UsersTable.passwordHash] = passwordHash
        }.insertedCount == 1
    }

    private fun ResultRow.toUser() = User(
        id = this[UsersTable.id],
        username = this[UsersTable.username],
        passwordHash = this[UsersTable.passwordHash],
    )
}
