package com.mauricioliveira.awesomenotes.core.domain.validation

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CredentialRulesTest {

    @Test
    fun `username must have between 3 and 50 characters`() {
        assertFalse(CredentialRules.isValidUsername("ab"))
        assertTrue(CredentialRules.isValidUsername("abc"))
        assertTrue(CredentialRules.isValidUsername("a".repeat(50)))
        assertFalse(CredentialRules.isValidUsername("a".repeat(51)))
    }

    @Test
    fun `password must have between 8 and 72 characters`() {
        assertFalse(CredentialRules.isValidPassword("a".repeat(7)))
        assertTrue(CredentialRules.isValidPassword("a".repeat(8)))
        assertTrue(CredentialRules.isValidPassword("a".repeat(72)))
        assertFalse(CredentialRules.isValidPassword("a".repeat(73)))
    }
}
