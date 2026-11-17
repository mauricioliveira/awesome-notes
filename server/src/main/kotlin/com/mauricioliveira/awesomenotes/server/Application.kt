package com.mauricioliveira.awesomenotes.server

import com.mauricioliveira.awesomenotes.server.config.AppConfig
import com.mauricioliveira.awesomenotes.server.database.DatabaseFactory
import com.mauricioliveira.awesomenotes.server.docs.docsRoutes
import com.mauricioliveira.awesomenotes.server.health.healthRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.routing.routing

fun main() {
    val config = AppConfig.fromEnv()
    DatabaseFactory.connect(config = config.database)
    embeddedServer(
        factory = Netty,
        port = config.port,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    install(CallLogging)
    routing {
        healthRoutes()
        docsRoutes()
    }
}