package com.highlightEditor.editor

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import com.highlightEditor.editor.docTree.DocumentElement
import com.highlightEditor.editor.draw.UnderlineDrawer
import com.highlightEditor.editor.text.OffsetState
import com.highlightEditor.editor.text.TextState
import kotlinx.coroutines.CoroutineScope

class EditorState(
    private val text: TextFieldValue,
    scope: CoroutineScope
) {
    val textState = TextState(text)
    val underlineDrawer = UnderlineDrawer(Color.Red, null, 0)
    //val diagnosticState = DiagnosticState(textState)
    val cursorPosition = OffsetState()
    var cursorPointsElement = mutableStateOf<DocumentElement?>(null)
    //val diagnosticPopupState = DiagnosticPopupState(scope)
    val scrollState = ScrollState(0)

    fun onCursorMove(offset: IntOffset) {
        cursorPosition.set(offset)

        val textOffset = textState.getOffsetForPosition(offset)
        cursorPointsElement.value = textState.getElementForOffset(textOffset)
    }
}