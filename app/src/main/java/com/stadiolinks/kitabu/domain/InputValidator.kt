package com.stadiolinks.kitabu.domain

sealed interface ValidationResult {
    data class Valid(val value: String) : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}

/** Sanitises and validates every piece of free text before it reaches the database. */
object InputValidator {
    const val MAX_NAME = 40
    const val MAX_TITLE = 80
    const val MAX_AUTHOR = 60
    const val MAX_CATEGORY = 30
    const val MAX_QUERY = 50

    /** Character used to escape LIKE wildcards, see BookDao.searchBooks. */
    private const val LIKE_ESCAPE = "!"

    private val nameRegex = Regex("^[\\p{L}][\\p{L} .'-]*$")

    /** Removes angle brackets and control characters, collapses whitespace, trims and limits length. */
    fun sanitize(raw: String, maxLength: Int): String =
        raw.replace(Regex("[<>]"), "")
            .replace(Regex("\\s+"), " ")
            .filterNot { it.isISOControl() }
            .trim()
            .take(maxLength)
            .trimEnd()

    fun validateUserName(raw: String): ValidationResult {
        val value = sanitize(raw, MAX_NAME)
        return when {
            value.length < 2 -> ValidationResult.Invalid("Enter your name (at least 2 characters)")
            !nameRegex.matches(value) ->
                ValidationResult.Invalid("Use letters, spaces, hyphens or apostrophes only")
            else -> ValidationResult.Valid(value)
        }
    }

    fun validateRequired(raw: String, label: String, maxLength: Int): ValidationResult {
        val value = sanitize(raw, maxLength)
        return if (value.isEmpty()) ValidationResult.Invalid("$label is required")
        else ValidationResult.Valid(value)
    }

    /** Escapes % and _ so user input is matched literally inside a LIKE pattern. */
    fun escapeLike(value: String): String =
        value.replace(LIKE_ESCAPE, LIKE_ESCAPE + LIKE_ESCAPE)
            .replace("%", LIKE_ESCAPE + "%")
            .replace("_", LIKE_ESCAPE + "_")
}
