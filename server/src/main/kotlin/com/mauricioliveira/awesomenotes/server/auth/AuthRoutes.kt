package com.mauricioliveira.awesomenotes.server.auth

import com.mauricioliveira.awesomenotes.server.error.UnauthorizedException
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val request = call.receive<CredentialsRequest>()
            authService.register(request.username, request.password)
            call.respond(HttpStatusCode.Created)
        }
        post("/login") {
            val request = call.receive<CredentialsRequest>()
            val token = authService.login(request.username, request.password)
            call.respond(LoginResponse(token))
        }
        post("/logout") {
            val token = call.bearerToken() ?: throw UnauthorizedException()
            authService.logout(token)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
