package com.mauricioliveira.awesomenotes.feature.auth.register

import androidx.lifecycle.SavedStateHandle
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.testing.FakeAuthRepository
import com.mauricioliveira.awesomenotes.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val viewModel = RegisterViewModel(SavedStateHandle(), repository)

    private fun fill(username: String = "maria", password: String = "s3cretpass", confirm: String = password) {
        viewModel.onIntent(RegisterIntent.UsernameChanged(username))
        viewModel.onIntent(RegisterIntent.PasswordChanged(password))
        viewModel.onIntent(RegisterIntent.ConfirmPasswordChanged(confirm))
    }

    @Test
    fun `field errors appear only after the first submit`() = runTest {
        fill(username = "ma", password = "short")
        assertFalse(viewModel.state.value.usernameInvalid)

        viewModel.onIntent(RegisterIntent.Submit)

        assertTrue(viewModel.state.value.usernameInvalid)
        assertTrue(viewModel.state.value.passwordInvalid)
        assertTrue(repository.registerCalls.isEmpty())
    }

    @Test
    fun `different passwords are not sent`() = runTest {
        fill(confirm = "something-else")

        viewModel.onIntent(RegisterIntent.Submit)

        assertTrue(viewModel.state.value.passwordsDiffer)
        assertTrue(repository.registerCalls.isEmpty())
    }

    @Test
    fun `registers and then logs in with the same credentials`() = runTest {
        fill(username = " maria ")

        viewModel.onIntent(RegisterIntent.Submit)

        assertEquals(listOf("maria" to "s3cretpass"), repository.registerCalls)
        assertEquals(listOf("maria" to "s3cretpass"), repository.loginCalls)
        assertTrue(repository.isLoggedIn.value)
    }

    @Test
    fun `a taken username shows the error and does not log in`() = runTest {
        repository.registerError = AppError.UsernameTaken
        fill()

        viewModel.onIntent(RegisterIntent.Submit)

        assertTrue(viewModel.state.value.usernameTaken)
        assertEquals(null, viewModel.state.value.formError)
        assertFalse(viewModel.state.value.isSubmitting)
        assertTrue(repository.loginCalls.isEmpty())
    }

    @Test
    fun `when only the login failed, a retry does not register again`() = runTest {
        repository.loginError = AppError.NoConnection
        fill()
        viewModel.onIntent(RegisterIntent.Submit)
        assertEquals(AppError.NoConnection, viewModel.state.value.formError)

        repository.loginError = null
        viewModel.onIntent(RegisterIntent.Submit)

        assertEquals(1, repository.registerCalls.size)
        assertEquals(2, repository.loginCalls.size)
        assertTrue(repository.isLoggedIn.value)
    }
}
