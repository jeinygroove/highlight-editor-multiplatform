package com.highlightEditor.editor.diagnostics

interface TextAnalyzer {
    suspend fun analyze(text: String): List<DiagnosticElement>
    suspend fun autocomplete(context: String, prefix: String): List<String>
}