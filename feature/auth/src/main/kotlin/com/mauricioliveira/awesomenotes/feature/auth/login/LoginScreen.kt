package com.mauricioliveira.awesomenotes.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
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
internal fun LoginScreen(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
    onCreateAccount: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    // Closing the keyboard first lets the user see the result (spinner, error banner).
    val submit = {
        focusManager.clearFocus()
        onIntent(LoginIntent.Submit)
    }

    Surface {
        AuthLayout(
            modifier = Modifier.safeDrawingPadding().padding(24.dp),
            footer = {
                AuthFooter(
                    question = stringResource(R.string.login_new_here),
                    action = stringResource(R.string.login_create_account),
                    enabled = !state.isSubmitting,
                    onClick = onCreateAccount,
                )
            },
        ) {
            Spacer(Modifier.height(48.dp))
            AppMark()
            Spacer(Modifier.height(20.dp))
            Text(text = stringResource(R.string.app_title), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(40.dp))

            Column(
                modifier = Modifier.alpha(if (state.isSubmitting) 0.6f else 1f),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                UsernameField(
                    value = state.username,
                    onValueChange = { onIntent(LoginIntent.UsernameChanged(it)) },
                    enabled = !state.isSubmitting,
                )
                PasswordField(
                    value = state.password,
                    onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
                    enabled = !state.isSubmitting,
                    label = stringResource(R.string.password),
                    imeAction = ImeAction.Done,
                    onDone = submit,
                )
                state.error?.let { ErrorBanner(stringResource(it.messageRes())) }
                SubmitButton(
                    text = stringResource(R.string.login_action),
                    submittingText = stringResource(R.string.login_submitting),
                    isSubmitting = state.isSubmitting,
                    enabled = state.canSubmit,
                    onClick = submit,
                )
            }
        }
    }
}

@Composable
private fun AppMark() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(UiR.drawable.ic_note),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(32.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginScreenPreview() {
    AwesomeNotesTheme {
        LoginScreen(state = LoginState(username = "maria", password = "s3cretpass"), onIntent = {}, onCreateAccount = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginScreenErrorPreview() {
    AwesomeNotesTheme {
        LoginScreen(
            state = LoginState(username = "maria", password = "s3cretpass", error = AppError.InvalidCredentials),
            onIntent = {},
            onCreateAccount = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginScreenSubmittingDarkPreview() {
    AwesomeNotesTheme(darkTheme = true) {
        LoginScreen(
            state = LoginState(username = "maria", password = "s3cretpass", isSubmitting = true),
            onIntent = {},
            onCreateAccount = {},
        )
    }
}
