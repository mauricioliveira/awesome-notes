package com.mauricioliveira.awesomenotes.core.data.repository

import com.mauricioliveira.awesomenotes.core.data.mapper.resultOf
import com.mauricioliveira.awesomenotes.core.data.session.LocalSession
import com.mauricioliveira.awesomenotes.core.domain.repository.AuthRepository
import com.mauricioliveira.awesomenotes.core.network.AuthApi
import com.mauricioliveira.awesomenotes.core.network.dto.CredentialsDto
import com.mauricioliveira.awesomenotes.core.storage.TokenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class DefaultAuthRepository(
    private val authApi: AuthApi,
    tokenStore: TokenStore,
    private val session: LocalSession,
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> =
        tokenStore.token
            .map { it != null }
            .distinctUntilChanged()

    override val username: Flow<String?> = tokenStore.username

    override suspend fun register(username: String, password: String): Result<Unit> =
        resultOf { authApi.register(CredentialsDto(username, password)) }

    override suspend fun login(username: String, password: String): Result<Unit> =
        resultOf {
            val response = authApi.login(CredentialsDto(username, password))
            session.start(response.token, username)
        }

    override suspend fun logout() {
        try {
            // Revokes the token on the server. A failure is ignored: the local session ends anyway.
            resultOf { authApi.logout() }
        } finally {
            session.clear()
        }
    }
}
