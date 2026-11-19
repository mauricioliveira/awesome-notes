package com.mauricioliveira.awesomenotes.core.domain.validation

/** The server's limits for a note. The editor stops accepting text past them. */
object NoteRules {

    const val TITLE_MAX_LENGTH = 255
    const val CONTENT_MAX_LENGTH = 10_000

    fun isValidTitle(title: String): Boolean = title.isNotBlank() && title.length <= TITLE_MAX_LENGTH
}
