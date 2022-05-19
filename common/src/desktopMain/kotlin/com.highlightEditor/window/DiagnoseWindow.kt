package com.highlightEditor.window

import androidx.compose.runtime.*
import androidx.compose.ui.text.TextRange
import com.highlightEditor.editor.diagnostics.DiagnosticPopup
import com.highlightEditor.editor.docTree.DocumentType
import kotlinx.coroutines.launch

@Composable
fun DiagnoseWindow(state: EditorWindowState) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.editorState.textState.text.text) {
        println("CALL DIAGNOSE")
        state.diagnoseText()
        println("END DIAGNOSE")
    }

    if (state.diagnosticState.diagnosticPopupState.isVisible) {
        println("HERE")
        DiagnosticPopup(
            offsetTop = 180f,//offsetTop,
            editorState = state.editorState,
            diagnosticState = state.diagnosticState,
            handleTextChange = { v, range, textFix ->
                val elems = state.editorState.textState.documentModel.getElementsInRange(TextRange(range.first, range.last))
                val type = if (elems.size != 1) DocumentType.TEXT else elems[0].type
                scope.launch { state.setText(v, type, range, textFix) }
            }
        )
    }
}