package com.mauricioliveira.awesomenotes.feature.notes.list

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mauricioliveira.awesomenotes.core.domain.model.AppError
import com.mauricioliveira.awesomenotes.core.domain.model.Note
import com.mauricioliveira.awesomenotes.core.ui.component.ErrorView
import com.mauricioliveira.awesomenotes.core.ui.error.messageRes
import com.mauricioliveira.awesomenotes.core.ui.theme.AwesomeNotesTheme
import com.mauricioliveira.awesomenotes.feature.notes.R
import com.mauricioliveira.awesomenotes.feature.notes.format.editedText
import java.time.Instant
import com.mauricioliveira.awesomenotes.core.ui.R as UiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotesListScreen(
    state: NotesListState,
    username: String?,
    onIntent: (NotesListIntent) -> Unit,
    onOpenNote: (String) -> Unit,
    onNewNote: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val refreshError = (state as? NotesListState.Content)?.refreshError
    val snackbarMessage = refreshError?.let {
        if (it == AppError.NoConnection) stringResource(R.string.notes_offline) else stringResource(R.string.notes_refresh_failed)
    }
    val retry = stringResource(UiR.string.retry)

    // Indefinite: the user is looking at old notes until a refresh works. A new refresh clears it.
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(snackbarMessage, actionLabel = retry, duration = SnackbarDuration.Indefinite)
        if (result == SnackbarResult.ActionPerformed) onIntent(NotesListIntent.Refresh)
    }

    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notes_title)) },
                actions = { AccountButton(username = username, onLogout = { showLogoutDialog = true }) },
            )
        },
        floatingActionButton = {
            if (state !is NotesListState.Loading) {
                ExtendedFloatingActionButton(
                    onClick = onNewNote,
                    icon = { Icon(painterResource(UiR.drawable.ic_add), contentDescription = null) },
                    text = { Text(stringResource(R.string.notes_new)) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val contentModifier = Modifier.fillMaxSize().padding(padding)
        when (state) {
            NotesListState.Loading -> LoadingNotes(contentModifier)
            is NotesListState.Error -> ErrorView(
                title = stringResource(R.string.notes_error_title),
                message = if (state.error == AppError.NoConnection) {
                    stringResource(R.string.notes_error_offline)
                } else {
                    stringResource(state.error.messageRes())
                },
                onRetry = { onIntent(NotesListIntent.Refresh) },
                modifier = contentModifier,
            )
            is NotesListState.Empty -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(NotesListIntent.Refresh) },
                modifier = contentModifier,
            ) {
                EmptyNotes()
            }
            is NotesListState.Content -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(NotesListIntent.Refresh) },
                modifier = contentModifier,
            ) {
                NotesList(notes = state.notes, onOpenNote = onOpenNote)
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                onIntent(NotesListIntent.Logout)
            },
            onDismiss = { showLogoutDialog = false },
        )
    }
}

@Composable
private fun NotesList(notes: List<Note>, onOpenNote: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Bottom space so the last card is not hidden behind the "New note" button.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCard(note = note, onClick = { onOpenNote(note.id) })
        }
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = editedText(note.updatedAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun LoadingNotes(modifier: Modifier) {
    val loading = stringResource(R.string.notes_loading)
    val alpha by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .alpha(alpha)
            .semantics { contentDescription = loading },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(0.45f to 0.7f, 0.6f to 0.85f, 0.35f to 0.55f, 0.5f to 0.8f, 0.4f to 0.65f).forEach { (title, line) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SkeletonLine(height = 16, fraction = title)
                SkeletonLine(height = 12, fraction = 1f)
                SkeletonLine(height = 12, fraction = line)
            }
        }
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

@Composable
private fun EmptyNotes() {
    // Scrollable so pull-to-refresh also works on the empty screen.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 40.dp)
            .padding(top = 160.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyIllustration()
        Text(stringResource(R.string.notes_empty_title), style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(R.string.notes_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Two tilted note cards, drawn with shapes instead of an image. */
@Composable
private fun EmptyIllustration() {
    val front = MaterialTheme.colorScheme.onPrimaryContainer
    Box(modifier = Modifier.size(width = 136.dp, height = 120.dp)) {
        Box(
            Modifier
                .offset(x = 14.dp, y = 14.dp)
                .size(width = 80.dp, height = 100.dp)
                .rotate(-8f)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(14.dp)),
        )
        Column(
            modifier = Modifier
                .offset(x = 40.dp, y = 4.dp)
                .size(width = 80.dp, height = 100.dp)
                .rotate(6f)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Box(Modifier.fillMaxWidth(0.7f).height(8.dp).alpha(0.55f).background(front, RoundedCornerShape(4.dp)))
            listOf(1f, 0.85f, 0.6f).forEach { width ->
                Box(Modifier.fillMaxWidth(width).height(6.dp).alpha(0.3f).background(front, RoundedCornerShape(3.dp)))
            }
        }
    }
}

@Composable
private fun AccountButton(username: String?, onLogout: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val name = username.orEmpty()
    val accountDescription = stringResource(R.string.account_button, name)
    Box {
        // The avatar is only a letter, so the button carries the description TalkBack reads.
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.semantics { contentDescription = accountDescription },
        ) {
            Avatar(name = name, size = 32)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(name = name, size = 40)
                Column {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.account_logged_in_here),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            DropdownMenuItem(
                text = { Text(stringResource(R.string.account_log_out)) },
                leadingIcon = { Icon(painterResource(UiR.drawable.ic_logout), contentDescription = null) },
                onClick = {
                    expanded = false
                    onLogout()
                },
            )
        }
    }
}

@Composable
private fun Avatar(name: String, size: Int) {
    Box(
        modifier = Modifier.size(size.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.uppercase().orEmpty(),
            color = MaterialTheme.colorScheme.onPrimary,
            style = if (size >= 40) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(painterResource(UiR.drawable.ic_logout), contentDescription = null) },
        title = { Text(stringResource(R.string.logout_title)) },
        text = { Text(stringResource(R.string.logout_message)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.account_log_out)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

private val previewNotes = listOf(
    Note("1", "Shopping list", "Milk, eggs, bread, coffee beans, olive oil and tomatoes for Saturday lunch.", Instant.now(), Instant.now()),
    Note("2", "Weekend in Gerês", "Leave Friday after work. Book the cabin near Caniçada.", Instant.now(), Instant.now().minusSeconds(86_400)),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NotesContentPreview() {
    AwesomeNotesTheme {
        NotesListScreen(NotesListState.Content(previewNotes, false, null), "maria", {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NotesLoadingPreview() {
    AwesomeNotesTheme { NotesListScreen(NotesListState.Loading, "maria", {}, {}, {}) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NotesEmptyPreview() {
    AwesomeNotesTheme { NotesListScreen(NotesListState.Empty(false), "maria", {}, {}, {}) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NotesErrorPreview() {
    AwesomeNotesTheme { NotesListScreen(NotesListState.Error(AppError.NoConnection), "maria", {}, {}, {}) }
}
