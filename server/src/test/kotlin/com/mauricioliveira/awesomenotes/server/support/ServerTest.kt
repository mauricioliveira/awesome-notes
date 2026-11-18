package com.mauricioliveira.awesomenotes.server.support

import com.mauricioliveira.awesomenotes.server.auth.AuthService
import com.mauricioliveira.awesomenotes.server.auth.PasswordHasher
import com.mauricioliveira.awesomenotes.server.database.SessionRepository
import com.mauricioliveira.awesomenotes.server.database.UserRepository
import com.mauricioliveira.awesomenotes.server.module
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun serverTest(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
    TestDatabase.reset()
    val database = TestDatabase.database
    val authService = AuthService(
        users = UserRepository(database),
        sessions = SessionRepository(database),
        passwordHasher = PasswordHasher(cost = 4),
    )
    application { module(authService) }
    block()
}

suspend fun HttpClient.postJson(path: String, json: String): HttpResponse =
    post(path) {
        contentType(ContentType.Application.Json)
        setBody(json)
    }

suspend fun HttpResponse.jsonField(name: String): String =
    Json.parseToJsonElement(bodyAsText()).jsonObject.getValue(name).jsonPrimitive.content
