package com.mauricioliveira.awesomenotes.core.network

import com.mauricioliveira.awesomenotes.core.network.dto.NewNoteDto
import com.mauricioliveira.awesomenotes.core.network.dto.NoteDto
import com.mauricioliveira.awesomenotes.core.network.dto.NoteInputDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class NotesApi(private val client: HttpClient) {

    suspend fun getNotes(): List<NoteDto> =
        client.get("notes").body()

    /** Returns the created note, or the existing one when this is a retry (200). */
    suspend fun createNote(note: NewNoteDto): NoteDto =
        client.post("notes") { setBody(note) }.body()

    suspend fun updateNote(id: String, input: NoteInputDto): NoteDto =
        client.put("notes/$id") { setBody(input) }.body()

    suspend fun deleteNote(id: String) {
        client.delete("notes/$id")
    }
}
