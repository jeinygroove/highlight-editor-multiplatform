package com.highlightEditor.editor.diagnostics

/**
 * Contains information about the diagnostic message and its position in the text.
 *
 * Offset enumeration starts from 0.
 */
data class DiagnosticElement(
    val offset: Int,
    val length: Int,
    val message: String,
    val suggestions: List<String> = listOf()
)
