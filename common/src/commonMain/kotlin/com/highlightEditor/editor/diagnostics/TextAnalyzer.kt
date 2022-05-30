package com.highlightEditor.editor.diagnostics

import com.highlightEditor.editor.text.Sentence

interface TextAnalyzer {
    suspend fun analyze(text: List<Sentence>): List<DiagnosticElement>
    suspend fun autocomplete(context: String, prefix: String): List<String>
}