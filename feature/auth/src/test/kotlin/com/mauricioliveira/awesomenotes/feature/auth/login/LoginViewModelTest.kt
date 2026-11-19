package com.mauricioliveira.awesomenotes.feature.auth.login

import androidx.lifecycle.SavedStateHandle
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.testing.FakeAuthRepository
import com.mauricioliveira.awesomenotes.core.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val savedStateHandle = SavedStateHandle()

    private fun viewModel() = LoginViewModel(savedStateHandle, repository)

    private fun LoginViewModel.fill(username: String = "maria", password: String = "s3cretpass") {
        onIntent(LoginIntent.UsernameChanged(username))
        onIntent(LoginIntent.PasswordChanged(password))
    }

    @Test
    fun `submit does nothing while a field is empty`() = runTest {
        val viewModel = viewModel()
        viewModel.fill(password = "")

        viewModel.onIntent(LoginIntent.Submit)

        assertFalse(viewModel.state.value.canSubmit)
        assertTrue(repository.loginCalls.isEmpty())
    }

    @Test
    fun `logs in with the trimmed username`() = runTest {
        val viewModel = viewModel()
        viewModel.fill(username = " maria ")

        viewModel.onIntent(LoginIntent.Submit)

        assertEquals(listOf("maria" to "s3cretpass"), repository.loginCalls)
        assertTrue(repository.isLoggedIn.value)
    }

    @Test
    fun `wrong credentials show the error and allow another try`() = runTest {
        repository.loginError = AppError.InvalidCredentials
        val viewModel = viewModel()
        viewModel.fill()

        viewModel.onIntent(LoginIntent.Submit)

        assertEquals(AppError.InvalidCredentials, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSubmitting)
        assertTrue(viewModel.state.value.canSubmit)
    }

    @Test
    fun `editing a field clears the error`() = runTest {
        repository.loginError = AppError.NoConnection
        val viewModel = viewModel()
        viewModel.fill()
        viewModel.onIntent(LoginIntent.Submit)

        viewModel.onIntent(LoginIntent.PasswordChanged("another"))

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `shows submitting and ignores a second submit while the request runs`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        val viewModel = viewModel()
        viewModel.fill()

        viewModel.onIntent(LoginIntent.Submit)
        viewModel.onIntent(LoginIntent.Submit)

        assertTrue(viewModel.state.value.isSubmitting)
        assertEquals(1, repository.loginCalls.size)
        gate.complete(Unit)
    }

    @Test
    fun `after process death the username comes back and the password does not`() = runTest {
        viewModel().fill()

        // A new ViewModel with the same SavedStateHandle is what Android gives back after process death.
        val restored = viewModel().state.value

        assertEquals("maria", restored.username)
        assertEquals("", restored.password)
    }
}
