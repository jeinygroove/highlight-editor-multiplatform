package com.highlightEditor.editor.diagnostics

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextFieldValue
import com.highlightEditor.editor.EditorState

@Composable
expect fun DiagnosticPopup(
    offsetTop: Float,
    editorState: EditorState,
    diagnosticState: DiagnosticState,
    handleTextChange: (TextFieldValue, IntRange, String) -> Unit = {_, _, _ -> }
)