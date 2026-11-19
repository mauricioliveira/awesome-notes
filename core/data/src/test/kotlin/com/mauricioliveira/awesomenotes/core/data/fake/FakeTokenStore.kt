package com.mauricioliveira.awesomenotes.core.data.fake

import com.mauricioliveira.awesomenotes.core.storage.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTokenStore(initial: String? = null) : TokenStore {

    override val token = MutableStateFlow(initial)

    override suspend fun save(token: String) {
        this.token.value = token
    }

    override suspend fun clear() {
        token.value = null
    }
}
