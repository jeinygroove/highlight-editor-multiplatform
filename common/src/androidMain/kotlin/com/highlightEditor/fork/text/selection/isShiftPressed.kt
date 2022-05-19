package com.highlightEditor.fork.text.selection

import androidx.compose.ui.input.pointer.PointerEvent

internal actual val PointerEvent.isShiftPressed: Boolean
    get() = false