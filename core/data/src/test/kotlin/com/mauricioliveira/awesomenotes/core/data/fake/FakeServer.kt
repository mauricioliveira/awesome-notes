package com.mauricioliveira.awesomenotes.core.data.fake

import com.mauricioliveira.awesomenotes.core.network.AuthApi
import com.mauricioliveira.awesomenotes.core.network.HttpClientFactory
import com.mauricioliveira.awesomenotes.core.network.NotesApi
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

/** Real AuthApi and NotesApi on top of a MockEngine. Each test sets [handler] to answer requests. */
class FakeServer(tokenStore: FakeTokenStore) {

    val requests = mutableListOf<HttpRequestData>()

    var handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { reply(HttpStatusCode.OK) }

    private val client = HttpClientFactory.create(
        engine = MockEngine { request ->
            requests += request
            handler(request)
        },
        baseUrl = "http://api.test/",
        tokenProvider = { tokenStore.token.value },
    )

    val authApi = AuthApi(client)
    val notesApi = NotesApi(client)
}

fun MockRequestHandleScope.reply(status: HttpStatusCode, body: String = "") =
    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

fun MockRequestHandleScope.replyError(status: HttpStatusCode, code: String) =
    reply(status, """{"code":"$code","message":"x"}""")

fun noteJson(id: String, title: String = "T", updatedAt: String = "2026-11-18T10:00:00Z") =
    """{"id":"$id","title":"$title","content":"C","createdAt":"2026-11-18T09:00:00Z","updatedAt":"$updatedAt"}"""
