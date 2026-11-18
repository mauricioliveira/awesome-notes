package com.mauricioliveira.awesomenotes.server.notes

import com.mauricioliveira.awesomenotes.server.database.Note
import kotlinx.serialization.Serializable

@Serializable
data class NewNoteRequest(val id: String, val title: String, val content: String)

@Serializable
data class NoteInputRequest(val title: String, val content: String)

@Serializable
data class NoteResponse(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
)

fun Note.toResponse() = NoteResponse(
    id = id.toString(),
    title = title,
    content = content,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)
