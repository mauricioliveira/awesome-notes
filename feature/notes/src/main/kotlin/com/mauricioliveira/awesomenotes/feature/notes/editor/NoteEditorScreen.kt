package com.mauricioliveira.awesomenotes.feature.notes.editor

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.ui.component.MessageView
import com.mauricioliveira.awesomenotes.core.ui.error.messageRes
import com.mauricioliveira.awesomenotes.core.ui.theme.AwesomeNotesTheme
import com.mauricioliveira.awesomenotes.feature.notes.R
import com.mauricioliveira.awesomenotes.feature.notes.format.editedSentence
import java.time.Instant
import com.mauricioliveira.awesomenotes.core.ui.R as UiR

@Composable
internal fun NoteEditorScreen(
    state: NoteEditorState,
    onIntent: (NoteEditorIntent) -> Unit,
    onClose: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val failureMessage = (state as? NoteEditorState.Editing)?.failed?.let { failure ->
        when {
            failure.error != AppError.NoConnection -> stringResource(failure.error.messageRes())
            failure.action == EditorAction.Save -> stringResource(R.string.editor_save_failed)
            else -> stringResource(R.string.editor_delete_failed)
        }
    }
    val retry = stringResource(UiR.string.retry)
    val focusManager = LocalFocusManager.current
    // Closing the keyboard first lets the user see the result (spinner, snackbar).
    val onEditorIntent: (NoteEditorIntent) -> Unit = { intent ->
        if (intent == NoteEditorIntent.Save || intent == NoteEditorIntent.Delete) focusManager.clearFocus()
        onIntent(intent)
    }

    // Indefinite: the text is not saved until a retry works. A new attempt clears the failure.
    LaunchedEffect(failureMessage) {
        if (failureMessage == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(failureMessage, actionLabel = retry, duration = SnackbarDuration.Indefinite)
        if (result == SnackbarResult.ActionPerformed) onIntent(NoteEditorIntent.Retry)
    }

    Scaffold(
        topBar = { EditorTopBar(state = state, onIntent = onEditorIntent) },
        // Above the keyboard and the footer, so a failure is never hidden while the user is typing.
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.imePadding().padding(bottom = 48.dp)) },
    ) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when (state) {
            NoteEditorState.Loading -> LoadingNote(modifier)
            NoteEditorState.NotFound -> MessageView(
                icon = UiR.drawable.ic_note_off,
                title = stringResource(R.string.editor_not_found_title),
                message = stringResource(R.string.editor_not_found_message),
                modifier = modifier,
            ) {
                FilledTonalButton(onClick = onClose, modifier = Modifier.height(48.dp)) {
                    Text(stringResource(R.string.editor_back_to_notes))
                }
            }
            // consumeWindowInsets: the keyboard inset already includes the navigation bar the Scaffold padded for.
            is NoteEditorState.Editing -> EditorForm(
                state = state,
                onIntent = onIntent,
                modifier = modifier.consumeWindowInsets(padding).imePadding(),
            )
            // The route is navigating back.
            NoteEditorState.Closed -> Unit
        }
    }

    if (state is NoteEditorState.Editing) EditorDialogs(state, onIntent)
}

@Composable
private fun EditorDialogs(state: NoteEditorState.Editing, onIntent: (NoteEditorIntent) -> Unit) {
    when (state.dialog) {
        EditorDialog.Discard -> AlertDialog(
            onDismissRequest = { onIntent(NoteEditorIntent.DismissDialog) },
            title = { Text(stringResource(R.string.discard_title)) },
            text = {
                Text(
                    if (state.isNew) {
                        stringResource(R.string.discard_message_new)
                    } else {
                        stringResource(R.string.discard_message, state.savedTitle)
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { onIntent(NoteEditorIntent.ConfirmDiscard) }) { Text(stringResource(R.string.discard_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(NoteEditorIntent.DismissDialog) }) { Text(stringResource(R.string.discard_keep_editing)) }
            },
        )
        EditorDialog.Delete -> AlertDialog(
            onDismissRequest = { onIntent(NoteEditorIntent.DismissDialog) },
            icon = { Icon(painterResource(UiR.drawable.ic_delete), contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.delete_title)) },
            text = { Text(stringResource(R.string.delete_message, state.savedTitle)) },
            confirmButton = {
                TextButton(
                    onClick = { onIntent(NoteEditorIntent.ConfirmDelete) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(NoteEditorIntent.DismissDialog) }) { Text(stringResource(R.string.cancel)) }
            },
        )
        null -> Unit
    }
}

@Composable
private fun EditorTopBar(state: NoteEditorState, onIntent: (NoteEditorIntent) -> Unit) {
    Surface {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 4.dp, end = 12.dp)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onIntent(NoteEditorIntent.Back) }) {
                Icon(painterResource(UiR.drawable.ic_arrow_back), contentDescription = stringResource(R.string.editor_back))
            }
            Spacer(Modifier.weight(1f))
            if (state is NoteEditorState.Editing) {
                if (!state.isNew) {
                    IconButton(onClick = { onIntent(NoteEditorIntent.Delete) }, enabled = state.canDelete) {
                        Icon(painterResource(UiR.drawable.ic_delete), contentDescription = stringResource(R.string.editor_delete))
                    }
                }
                val saving = state.inProgress == EditorAction.Save
                Button(
                    onClick = { onIntent(NoteEditorIntent.Save) },
                    enabled = state.canSave,
                    colors = if (saving) {
                        ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                    modifier = Modifier.padding(start = 4.dp).height(40.dp),
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.editor_saving))
                    } else {
                        Text(stringResource(R.string.editor_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorForm(state: NoteEditorState.Editing, onIntent: (NoteEditorIntent) -> Unit, modifier: Modifier) {
    val busy = state.inProgress != null
    val textColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    val titleLabel = stringResource(R.string.editor_title_hint)
    val contentLabel = stringResource(R.string.editor_content_hint)
    val titleFocus = remember { FocusRequester() }
    // A new note starts with the cursor in the title. rememberSaveable: not again after rotation.
    var focusedOnce by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (state.isNew && !focusedOnce) {
            titleFocus.requestFocus()
            focusedOnce = true
        }
    }

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(if (busy) 0.6f else 1f)
                .padding(horizontal = 24.dp)
                .padding(top = 4.dp),
        ) {
            BasicTextField(
                value = state.title,
                onValueChange = { onIntent(NoteEditorIntent.TitleChanged(it)) },
                readOnly = busy,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = textColor),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .focusRequester(titleFocus)
                    .semantics { contentDescription = titleLabel },
                decorationBox = { field ->
                    Box {
                        if (state.title.isEmpty()) {
                            Text(titleLabel, style = MaterialTheme.typography.headlineSmall, color = placeholderColor)
                        }
                        field()
                    }
                },
            )
            if (state.titleMissing) {
                HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.error)
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(UiR.drawable.ic_error),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.editor_title_missing),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            BasicTextField(
                value = state.content,
                onValueChange = { onIntent(NoteEditorIntent.ContentChanged(it)) },
                readOnly = busy,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 12.dp)
                    .semantics { contentDescription = contentLabel },
                decorationBox = { field ->
                    Box {
                        if (state.content.isEmpty()) {
                            Text(contentLabel, style = MaterialTheme.typography.bodyLarge, color = placeholderColor)
                        }
                        field()
                    }
                },
            )
        }
        EditorFooter(state)
    }
}

@Composable
private fun EditorFooter(state: NoteEditorState.Editing) {
    val saveFailed = state.failed?.action == EditorAction.Save
    val text = when {
        state.updatedAt == null && (state.titleMissing || saveFailed) -> stringResource(R.string.editor_new_not_saved)
        state.updatedAt == null -> stringResource(R.string.editor_new_note)
        saveFailed -> stringResource(R.string.editor_changes_not_saved, editedSentence(state.updatedAt))
        else -> editedSentence(state.updatedAt)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LoadingNote(modifier: Modifier) {
    val loading = stringResource(R.string.editor_loading)
    val alpha by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .alpha(alpha)
            .semantics { contentDescription = loading },
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonLine(height = 28, fraction = 0.58f)
        Spacer(Modifier.height(14.dp))
        listOf(1f, 0.92f, 0.96f, 0.64f).forEach { SkeletonLine(height = 14, fraction = it) }
    }
}

@Composable
private fun SkeletonLine(height: Int, fraction: Float) {
    Box(
        Modifier
            .fillMaxWidth(fraction)
            .height(height.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(height.dp / 2)),
    )
}

private val previewState = NoteEditorState.Editing(
    isNew = false,
    title = "Shopping list",
    content = "Milk\nEggs\nBread\nCoffee beans",
    savedTitle = "Shopping list",
    savedContent = "Milk\nEggs",
    updatedAt = Instant.now(),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun EditorPreview() {
    AwesomeNotesTheme { NoteEditorScreen(previewState, {}, {}) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun EditorSaveFailedPreview() {
    AwesomeNotesTheme {
        NoteEditorScreen(previewState.copy(failed = EditorFailure(EditorAction.Save, AppError.NoConnection)), {}, {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun EditorNewTitleMissingPreview() {
    AwesomeNotesTheme {
        NoteEditorScreen(
            NoteEditorState.Editing(
                isNew = true,
                title = "",
                content = "Call the dentist.",
                savedTitle = "",
                savedContent = "",
                updatedAt = null,
                titleMissing = true,
            ),
            {},
            {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun EditorNotFoundPreview() {
    AwesomeNotesTheme { NoteEditorScreen(NoteEditorState.NotFound, {}, {}) }
}
