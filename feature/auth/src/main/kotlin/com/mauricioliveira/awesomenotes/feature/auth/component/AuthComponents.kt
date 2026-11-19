package com.mauricioliveira.awesomenotes.feature.auth.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mauricioliveira.awesomenotes.feature.auth.R
import com.mauricioliveira.awesomenotes.core.ui.R as UiR

/**
 * Scrollable screen that is at least as tall as the window: [content] at the top and [footer] at the bottom,
 * staying above the keyboard. With a tall keyboard the footer simply scrolls.
 */
@Composable
internal fun AuthLayout(
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().imePadding()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                content = content,
            )
            footer()
        }
    }
}

/** "Question? Action" line at the bottom of the auth screens. */
@Composable
internal fun AuthFooter(
    question: String,
    action: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(action)
        }
    }
}

@Composable
internal fun UsernameField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    helper: String? = null,
    error: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(stringResource(R.string.username)) },
        isError = error != null,
        supportingText = (error ?: helper)?.let { { Text(it) } },
        trailingIcon = if (error != null) {
            { Icon(painterResource(UiR.drawable.ic_error), contentDescription = null) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
    )
}

/** Password field with a show/hide button. Visibility survives rotation but resets on a new screen. */
@Composable
internal fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    label: String,
    imeAction: ImeAction,
    onDone: () -> Unit = {},
    helper: String? = null,
    error: String? = null,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        isError = error != null,
        supportingText = (error ?: helper)?.let { { Text(it) } },
        trailingIcon = {
            // Out of the keyboard focus order, so "Next" goes to the next field and not to this button.
            // TalkBack still reaches it: it follows semantics, not input focus.
            IconButton(
                onClick = { visible = !visible },
                modifier = Modifier.focusProperties { canFocus = false },
            ) {
                Icon(
                    painter = painterResource(if (visible) UiR.drawable.ic_visibility_off else UiR.drawable.ic_visibility),
                    contentDescription = stringResource(if (visible) R.string.hide_password else R.string.show_password),
                )
            }
        },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
    )
}

/**
 * Full width pill button. While [isSubmitting] it keeps its colours and shows a spinner and [submittingText];
 * the form around it is dimmed instead. When [enabled] is false it uses the normal disabled colours.
 */
@Composable
internal fun SubmitButton(
    text: String,
    submittingText: String,
    isSubmitting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = if (isSubmitting) {
        ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary,
        )
    } else {
        ButtonDefaults.buttonColors()
    }
    Button(
        onClick = onClick,
        enabled = enabled && !isSubmitting,
        colors = colors,
        modifier = modifier.fillMaxWidth().height(48.dp),
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.width(8.dp))
            Text(submittingText)
        } else {
            Text(text)
        }
    }
}
