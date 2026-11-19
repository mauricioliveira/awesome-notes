package com.mauricioliveira.awesomenotes.core.testing

import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.domain.repository.AuthRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {

    override val isLoggedIn = MutableStateFlow(false)

    var registerError: AppError? = null
    var loginError: AppError? = null

    /** When set, register and login wait for it, so a test can look at the state mid-request. */
    var gate: CompletableDeferred<Unit>? = null

    val registerCalls = mutableListOf<Pair<String, String>>()
    val loginCalls = mutableListOf<Pair<String, String>>()
    var logoutCalls = 0
        private set

    override suspend fun register(username: String, password: String): Result<Unit> {
        registerCalls += username to password
        gate?.await()
        return registerError?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    override suspend fun login(username: String, password: String): Result<Unit> {
        loginCalls += username to password
        gate?.await()
        loginError?.let { return Result.failure(it) }
        isLoggedIn.value = true
        return Result.success(Unit)
    }

    override suspend fun logout() {
        logoutCalls++
        isLoggedIn.value = false
    }
}
