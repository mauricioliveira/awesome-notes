package com.mauricioliveira.awesomenotes.core.data.fake

import com.mauricioliveira.awesomenotes.core.storage.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTokenStore(initial: String? = null) : TokenStore {

    override val token = MutableStateFlow(initial)
    override val username = MutableStateFlow(initial?.let { "maria" })

    override suspend fun save(token: String, username: String) {
        this.token.value = token
        this.username.value = username
    }

    override suspend fun clear() {
        token.value = null
        username.value = null
    }
}
