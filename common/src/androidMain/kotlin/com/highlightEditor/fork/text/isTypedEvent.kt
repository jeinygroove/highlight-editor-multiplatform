package com.highlightEditor.fork.text

import androidx.compose.ui.input.key.KeyEvent

internal actual val KeyEvent.isTypedEvent: Boolean
    get() = false