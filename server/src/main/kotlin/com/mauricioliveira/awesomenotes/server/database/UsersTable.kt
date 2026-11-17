package com.mauricioliveira.awesomenotes.server.database

import org.jetbrains.exposed.v1.core.Table

object UsersTable : Table("users") {
    val id = long("id").autoIncrement()
    val username = varchar("username", length = 50).uniqueIndex()
    val passwordHash = varchar("password_hash", length = 60)

    override val primaryKey = PrimaryKey(id)
}