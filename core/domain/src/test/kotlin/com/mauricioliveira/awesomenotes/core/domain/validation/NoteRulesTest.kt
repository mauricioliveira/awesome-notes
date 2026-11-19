package com.mauricioliveira.awesomenotes.core.domain.validation

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NoteRulesTest {

    @Test
    fun `title must not be blank and must fit the server limit`() {
        assertFalse(NoteRules.isValidTitle("   "))
        assertTrue(NoteRules.isValidTitle("a"))
        assertTrue(NoteRules.isValidTitle("a".repeat(NoteRules.TITLE_MAX_LENGTH)))
        assertFalse(NoteRules.isValidTitle("a".repeat(NoteRules.TITLE_MAX_LENGTH + 1)))
    }
}
