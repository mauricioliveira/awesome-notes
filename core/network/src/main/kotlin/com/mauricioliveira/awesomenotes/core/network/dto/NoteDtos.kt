package com.mauricioliveira.awesomenotes.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class NewNoteDto(val id: String, val title: String, val content: String)

@Serializable
data class NoteInputDto(val title: String, val content: String)
