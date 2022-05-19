package com.highlightEditor.editor.diagnostics

import androidx.compose.runtime.mutableStateOf

class AutocompletionState {
    val autocompletion = mutableStateOf<String>("")
}