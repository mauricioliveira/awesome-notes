package com.mauricioliveira.awesomenotes.core.data.mapper

import com.mauricioliveira.awesomenotes.core.database.entity.NoteEntity
import com.mauricioliveira.awesomenotes.core.domain.model.Note
import com.mauricioliveira.awesomenotes.core.network.dto.NoteDto
import java.time.Instant

internal fun NoteDto.toEntity() = NoteEntity(
    id = id,
    title = title,
    content = content,
    createdAtMillis = Instant.parse(createdAt).toEpochMilli(),
    updatedAtMillis = Instant.parse(updatedAt).toEpochMilli(),
)

internal fun NoteEntity.toDomain() = Note(
    id = id,
    title = title,
    content = content,
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtMillis),
)
