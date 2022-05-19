package com.highlightEditor.fork.text

import androidx.compose.runtime.Composable
import com.highlightEditor.fork.text.selection.SelectionManager
import com.highlightEditor.fork.text.selection.TextFieldSelectionManager

@Composable
internal actual fun ContextMenuArea(
    manager: TextFieldSelectionManager,
    content: @Composable () -> Unit
) {
    content()
}

@Composable
internal actual fun ContextMenuArea(
    manager: SelectionManager,
    content: @Composable () -> Unit
) {
    content()
}