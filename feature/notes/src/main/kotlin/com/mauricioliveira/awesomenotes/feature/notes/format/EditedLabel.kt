package com.mauricioliveira.awesomenotes.feature.notes.format

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** When a note was last edited, relative to now, as the design shows it. */
sealed interface EditedLabel {
    /** "Today, 10:20" */
    data class Today(val time: LocalTime) : EditedLabel

    /** "Yesterday" */
    data object Yesterday : EditedLabel

    /** "15 Nov", or "15 Nov 2025" when [sameYear] is false. */
    data class Earlier(val date: LocalDate, val sameYear: Boolean) : EditedLabel
}

/** Pure, so it is tested with fixed instants and zones instead of the device clock. */
fun editedLabel(updatedAt: Instant, now: Instant, zone: ZoneId): EditedLabel {
    val edited = updatedAt.atZone(zone)
    val today = now.atZone(zone).toLocalDate()
    return when (edited.toLocalDate()) {
        today -> EditedLabel.Today(edited.toLocalTime())
        today.minusDays(1) -> EditedLabel.Yesterday
        else -> EditedLabel.Earlier(edited.toLocalDate(), sameYear = edited.year == today.year)
    }
}
