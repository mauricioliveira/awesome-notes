package com.mauricioliveira.awesomenotes.core.testing

import com.mauricioliveira.awesomenotes.core.domain.model.Note
import java.time.Instant

fun aNote(
    id: String = "note-1",
    title: String = "Groceries",
    content: String = "Milk, eggs",
    updatedAt: Instant = Instant.parse("2026-11-19T10:00:00Z"),
) = Note(
    id = id,
    title = title,
    content = content,
    createdAt = Instant.parse("2026-11-19T09:00:00Z"),
    updatedAt = updatedAt,
)
