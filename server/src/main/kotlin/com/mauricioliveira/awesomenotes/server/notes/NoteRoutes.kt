package com.mauricioliveira.awesomenotes.server.notes

import com.mauricioliveira.awesomenotes.server.auth.AuthService
import com.mauricioliveira.awesomenotes.server.auth.authenticatedUserId
import com.mauricioliveira.awesomenotes.server.error.NoteNotFoundException
import com.mauricioliveira.awesomenotes.server.error.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID

fun Route.noteRoutes(authService: AuthService, noteService: NoteService) {
    route("/notes") {
        get {
            val userId = call.authenticatedUserId(authService)
            call.respond(noteService.list(userId).map { it.toResponse() })
        }
        post {
            val userId = call.authenticatedUserId(authService)
            val request = call.receive<NewNoteRequest>()
            val id = request.id.toUuidOrNull() ?: throw ValidationException("Note id must be a UUID.")
            val result = noteService.create(userId, id, request.title, request.content)
            val status = if (result.created) HttpStatusCode.Created else HttpStatusCode.OK
            call.respond(status, result.note.toResponse())
        }
        put("/{id}") {
            val userId = call.authenticatedUserId(authService)
            val id = call.noteId()
            val request = call.receive<NoteInputRequest>()
            call.respond(noteService.update(userId, id, request.title, request.content).toResponse())
        }
        delete("/{id}") {
            val userId = call.authenticatedUserId(authService)
            noteService.delete(userId, call.noteId())
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

// An id that is not a UUID cannot match any note.
private fun ApplicationCall.noteId(): UUID =
    parameters["id"]?.toUuidOrNull() ?: throw NoteNotFoundException()

private fun String.toUuidOrNull(): UUID? =
    runCatching { UUID.fromString(this) }.getOrNull()
