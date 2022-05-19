package com.highlightEditor.fork.text

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.type

internal actual fun KeyEvent.cancelsTextSelection(): Boolean {
    return nativeKeyEvent.keyCode == NativeKeyEvent.KEYCODE_BACK && type == KeyEventType.KeyUp
}

// It's platform-specific behavior, Android doesn't have such a concept
internal actual fun showCharacterPalette() { }