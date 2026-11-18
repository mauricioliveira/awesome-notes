package com.mauricioliveira.awesomenotes.server.database

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp

object NotesTable : Table("notes") {
    val id = javaUUID("id")
    val userId = long("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE).index()
    val title = varchar("title", length = 255)
    val content = text("content")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
