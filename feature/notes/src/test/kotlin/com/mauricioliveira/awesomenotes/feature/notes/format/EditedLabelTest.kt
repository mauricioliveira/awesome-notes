package com.mauricioliveira.awesomenotes.feature.notes.format

import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.assertEquals

class EditedLabelTest {

    private val lisbon = ZoneId.of("Europe/Lisbon")
    private val now = Instant.parse("2026-11-19T10:00:00Z") // 10:00 in Lisbon (winter, UTC+0)

    @Test
    fun `earlier today shows the time`() {
        assertEquals(
            EditedLabel.Today(LocalTime.of(8, 30)),
            editedLabel(Instant.parse("2026-11-19T08:30:00Z"), now, lisbon),
        )
    }

    @Test
    fun `the day before shows yesterday, even a few minutes before midnight`() {
        assertEquals(EditedLabel.Yesterday, editedLabel(Instant.parse("2026-11-18T23:55:00Z"), now, lisbon))
    }

    @Test
    fun `the day is the one in the user's time zone, not in UTC`() {
        // 23:30 UTC on the 18th is already the 19th in Tokyo.
        val tokyo = ZoneId.of("Asia/Tokyo")
        val label = editedLabel(Instant.parse("2026-11-18T23:30:00Z"), Instant.parse("2026-11-19T03:00:00Z"), tokyo)
        assertEquals(EditedLabel.Today(LocalTime.of(8, 30)), label)
    }

    @Test
    fun `older dates show the day, with the year only when it differs`() {
        assertEquals(
            EditedLabel.Earlier(LocalDate.of(2026, 11, 2), sameYear = true),
            editedLabel(Instant.parse("2026-11-02T12:00:00Z"), now, lisbon),
        )
        assertEquals(
            EditedLabel.Earlier(LocalDate.of(2025, 12, 31), sameYear = false),
            editedLabel(Instant.parse("2025-12-31T12:00:00Z"), now, lisbon),
        )
    }
}
