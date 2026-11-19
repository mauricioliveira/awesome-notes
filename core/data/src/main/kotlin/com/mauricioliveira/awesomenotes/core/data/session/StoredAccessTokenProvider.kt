package com.mauricioliveira.awesomenotes.core.data.session

import com.mauricioliveira.awesomenotes.core.network.AccessTokenProvider
import com.mauricioliveira.awesomenotes.core.storage.TokenStore
import kotlinx.coroutines.flow.first

/** Gives the HTTP client the token kept in [TokenStore]. */
internal class StoredAccessTokenProvider(
    private val tokenStore: TokenStore,
) : AccessTokenProvider {

    override suspend fun accessToken(): String? = tokenStore.token.first()
}
