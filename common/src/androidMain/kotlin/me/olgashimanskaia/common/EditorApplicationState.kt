package me.olgashimanskaia.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.highlightEditor.editor.diagnostics.TextAnalyzer
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberApplicationState(analyzer: TextAnalyzer): EditorApplicationState {
    val scope = rememberCoroutineScope()
    val diagnosticScope = rememberCoroutineScope()
    return remember {
        EditorApplicationState(analyzer).apply {
            newWindow(scope, diagnosticScope)
        }
    }
}

class EditorApplicationState(val analyzer: TextAnalyzer) {
    private val _windows = mutableStateListOf<EditorWindowState>()
    val windows: List<EditorWindowState> get() = _windows

    fun newWindow(scope: CoroutineScope, diagnosticScope: CoroutineScope) {
        _windows.add(
            EditorWindowState(
                application = this,
                scope = scope,
                diagnosticScope = diagnosticScope,
                path = null,
                exit = _windows::remove
            )
        )
    }
}