package com.mauricioliveira.awesomenotes.feature.notes.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mauricioliveira.awesomenotes.feature.notes.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
private val dayMonthFormat = DateTimeFormatter.ofPattern("d MMM")
private val fullDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy")

/** "Today, 10:20", "Yesterday" or "15 Nov", for the list. */
@Composable
internal fun editedText(updatedAt: Instant): String =
    when (val label = editedLabel(updatedAt, Instant.now(), ZoneId.systemDefault())) {
        is EditedLabel.Today -> stringResource(R.string.edited_today, label.time.format(timeFormat))
        EditedLabel.Yesterday -> stringResource(R.string.edited_yesterday)
        is EditedLabel.Earlier -> label.date.format(if (label.sameYear) dayMonthFormat else fullDateFormat)
    }

/** "Edited today, 10:20", "Edited yesterday" or "Edited 15 Nov", for the editor footer. */
@Composable
internal fun editedSentence(updatedAt: Instant): String =
    when (val label = editedLabel(updatedAt, Instant.now(), ZoneId.systemDefault())) {
        is EditedLabel.Today -> stringResource(R.string.editor_edited_today, label.time.format(timeFormat))
        EditedLabel.Yesterday -> stringResource(R.string.editor_edited_yesterday)
        is EditedLabel.Earlier ->
            stringResource(R.string.editor_edited_on, label.date.format(if (label.sameYear) dayMonthFormat else fullDateFormat))
    }
