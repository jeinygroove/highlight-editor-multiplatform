package com.highlightEditor.editor.diagnostics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
class DiagnosticPopupState(
    private val scope: CoroutineScope
) {
    var isVisible by mutableStateOf(false)
    var placement by mutableStateOf(IntOffset.Zero)
    private val delay = 500L
    private var job: Job? = null
    var diagnosticElement: DiagnosticElement? = null
        private set

    fun setDiagnosticElement(element: DiagnosticElement?) {
         if (this.diagnosticElement != element) {
            job?.cancel()
            isVisible = false
            job = scope.launch {
                delay(delay)
                isVisible = true
            }
            this.diagnosticElement = element
        }
    }

    fun hide() {
        job?.cancel()
        isVisible = false
    }
}
