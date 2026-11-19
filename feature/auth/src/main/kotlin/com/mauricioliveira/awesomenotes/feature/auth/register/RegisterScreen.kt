package com.mauricioliveira.awesomenotes.feature.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.domain.validation.CredentialRules
import com.mauricioliveira.awesomenotes.core.ui.component.ErrorBanner
import com.mauricioliveira.awesomenotes.core.ui.error.messageRes
import com.mauricioliveira.awesomenotes.core.ui.theme.AwesomeNotesTheme
import com.mauricioliveira.awesomenotes.feature.auth.R
import com.mauricioliveira.awesomenotes.feature.auth.component.AuthFooter
import com.mauricioliveira.awesomenotes.feature.auth.component.AuthLayout
import com.mauricioliveira.awesomenotes.feature.auth.component.PasswordField
import com.mauricioliveira.awesomenotes.feature.auth.component.SubmitButton
import com.mauricioliveira.awesomenotes.feature.auth.component.UsernameField
import com.mauricioliveira.awesomenotes.core.ui.R as UiR

@Composable
internal fun RegisterScreen(
    state: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
    onBack: () -> Unit,
) {
    val usernameLength = CredentialRules.USERNAME_LENGTH
    val passwordLength = CredentialRules.PASSWORD_LENGTH

    val focusManager = LocalFocusManager.current
    // Closing the keyboard first lets the user see the result (spinner, error banner).
    val submit = {
        focusManager.clearFocus()
        onIntent(RegisterIntent.Submit)
    }

    Surface {
        Column(Modifier.safeDrawingPadding()) {
            IconButton(onClick = onBack, enabled = !state.isSubmitting, modifier = Modifier.padding(4.dp)) {
                Icon(
                    painter = painterResource(UiR.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.register_back),
                )
            }
            AuthLayout(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 8.dp),
                footer = {
                    AuthFooter(
                        question = stringResource(R.string.register_have_account),
                        action = stringResource(R.string.register_log_in),
                        enabled = !state.isSubmitting,
                        onClick = onBack,
                    )
                },
            ) {
                Text(text = stringResource(R.string.register_title), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.register_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(32.dp))

                Column(
                    modifier = Modifier.alpha(if (state.isSubmitting) 0.6f else 1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    UsernameField(
                        value = state.username,
                        onValueChange = { onIntent(RegisterIntent.UsernameChanged(it)) },
                        enabled = !state.isSubmitting,
                        helper = stringResource(R.string.register_username_helper, usernameLength.first, usernameLength.last),
                        error = when {
                            state.usernameInvalid ->
                                stringResource(R.string.register_username_invalid, usernameLength.first, usernameLength.last)
                            state.usernameTaken -> stringResource(AppError.UsernameTaken.messageRes())
                            else -> null
                        },
                    )
                    PasswordField(
                        value = state.password,
                        onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
                        enabled = !state.isSubmitting,
                        label = stringResource(R.string.password),
                        imeAction = ImeAction.Next,
                        helper = stringResource(R.string.register_password_helper, passwordLength.first),
                        error = if (state.passwordInvalid) {
                            stringResource(R.string.register_password_invalid, passwordLength.first, passwordLength.last)
                        } else {
                            null
                        },
                    )
                    PasswordField(
                        value = state.confirmPassword,
                        onValueChange = { onIntent(RegisterIntent.ConfirmPasswordChanged(it)) },
                        enabled = !state.isSubmitting,
                        label = stringResource(R.string.register_confirm_password),
                        imeAction = ImeAction.Done,
                        onDone = submit,
                        helper = stringResource(R.string.register_confirm_helper),
                        error = if (state.passwordsDiffer) stringResource(R.string.register_passwords_differ) else null,
                    )
                    state.formError?.let { ErrorBanner(stringResource(it.messageRes())) }
                    SubmitButton(
                        text = stringResource(R.string.register_action),
                        submittingText = stringResource(R.string.register_submitting),
                        isSubmitting = state.isSubmitting,
                        onClick = submit,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RegisterScreenPreview() {
    AwesomeNotesTheme {
        RegisterScreen(
            state = RegisterState(username = "maria", password = "s3cretpass", confirmPassword = "s3cretpass"),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RegisterScreenInvalidPreview() {
    AwesomeNotesTheme {
        RegisterScreen(
            state = RegisterState(username = "ma", password = "pass12", confirmPassword = "pass123", submitAttempted = true),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RegisterScreenTakenPreview() {
    AwesomeNotesTheme {
        RegisterScreen(
            state = RegisterState(
                username = "maria",
                password = "s3cretpass",
                confirmPassword = "s3cretpass",
                error = AppError.UsernameTaken,
            ),
            onIntent = {},
            onBack = {},
        )
    }
}
