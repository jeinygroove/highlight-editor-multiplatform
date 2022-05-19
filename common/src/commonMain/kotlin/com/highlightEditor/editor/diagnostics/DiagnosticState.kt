package com.highlightEditor.editor.diagnostics

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import com.highlightEditor.editor.text.TextState
import kotlinx.coroutines.CoroutineScope

/**
 * Responsible for text diagnostics, keeping and updating its results.
 */
class DiagnosticState(
    private val textState: TextState,
    scope: CoroutineScope
) {
    val diagnostics = mutableStateListOf<DiagnosticElement>()
    val diagnosticPopupState = DiagnosticPopupState(scope)
    val autocompletion = Autocomplete()

    fun setAutocomplete(autocomplete: String, prefix: String) {
        autocompletion.setAutocomplete(autocomplete, prefix)
    }

    fun updateAutocomplete(prefix: String): Boolean {
        return autocompletion.updateAutocomplete(prefix)
    }

    fun updateList(list: List<DiagnosticElement>) {
        diagnostics.clear()
        diagnostics.addAll(list)
    }

    fun findHoveredElement(offset: Int): DiagnosticElement? {
        return diagnostics.firstOrNull { elem ->
            elem.offset <= offset && elem.offset + elem.length > offset
        }
    }

    fun onCursorMove(offset: IntOffset) {
        diagnosticPopupState.hide()
        val caretOffset = textState.getOffsetForPosition(offset)
        val diagnosticHovered: DiagnosticElement? = findHoveredElement(caretOffset)

        if (diagnosticHovered != null) {
            diagnosticPopupState.setDiagnosticElement(diagnosticHovered)
            val startRangeOffset: Offset = textState.textLayoutResult?.getCursorRect(diagnosticHovered.offset)?.bottomLeft ?: Offset.Zero
            diagnosticPopupState.placement = IntOffset(startRangeOffset.x.toInt(), startRangeOffset.y.toInt())
            diagnosticPopupState.isVisible = true
        } else {
            diagnosticPopupState.setDiagnosticElement(null)
            diagnosticPopupState.hide()
        }
    }
}

class Autocomplete {
    val autocomplete = mutableStateOf("")
    val prefix = mutableStateOf("")

    fun setAutocomplete(autocomplete: String, prefix: String) {
        this.autocomplete.value = autocomplete
        this.prefix.value = prefix
    }

    // return True if autocomplete matches new text
    fun updateAutocomplete(prefix: String): Boolean {
        val diff = prefix.length - this.prefix.value.length
        if (prefix == this.prefix.value || this.autocomplete.value.isEmpty()) return false
        if (prefix.startsWith(this.prefix.value) && prefix.endsWith(autocomplete.value.substring(0, diff), false)) {
            autocomplete.value = autocomplete.value.substring(diff)
            this.prefix.value = prefix
            return true
        } else {
            this.autocomplete.value = ""
            this.prefix.value = ""
        }
        return false
    }
}