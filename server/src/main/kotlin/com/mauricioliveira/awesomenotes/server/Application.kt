package com.mauricioliveira.awesomenotes.server

import com.mauricioliveira.awesomenotes.server.auth.AuthService
import com.mauricioliveira.awesomenotes.server.auth.PasswordHasher
import com.mauricioliveira.awesomenotes.server.auth.authRoutes
import com.mauricioliveira.awesomenotes.server.config.AppConfig
import com.mauricioliveira.awesomenotes.server.database.DatabaseFactory
import com.mauricioliveira.awesomenotes.server.database.NoteRepository
import com.mauricioliveira.awesomenotes.server.database.SessionRepository
import com.mauricioliveira.awesomenotes.server.database.UserRepository
import com.mauricioliveira.awesomenotes.server.docs.docsRoutes
import com.mauricioliveira.awesomenotes.server.error.configureErrorHandling
import com.mauricioliveira.awesomenotes.server.health.healthRoutes
import com.mauricioliveira.awesomenotes.server.notes.NoteService
import com.mauricioliveira.awesomenotes.server.notes.noteRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing

fun main() {
    val config = AppConfig.fromEnv()
    val database = DatabaseFactory.connect(config = config.database)
    val authService = AuthService(
        users = UserRepository(database),
        sessions = SessionRepository(database),
        passwordHasher = PasswordHasher(),
    )
    val noteService = NoteService(NoteRepository(database))

    embeddedServer(factory = Netty, port = config.port, host = "0.0.0.0") {
        module(authService, noteService)
    }.start(wait = true)
}

fun Application.module(authService: AuthService, noteService: NoteService) {
    install(CallLogging)
    install(ContentNegotiation) { json() }
    configureErrorHandling()
    routing {
        healthRoutes()
        docsRoutes()
        authRoutes(authService)
        noteRoutes(authService, noteService)
    }
}
