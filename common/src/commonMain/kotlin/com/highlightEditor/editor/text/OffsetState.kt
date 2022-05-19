package com.highlightEditor.editor.text

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset

class OffsetState() {
    companion object {
        val Unspecified = IntOffset(-1, -1)
    }

    var value by mutableStateOf(Unspecified)
        private set

    fun set(offset: IntOffset) {
        value = offset
    }
}