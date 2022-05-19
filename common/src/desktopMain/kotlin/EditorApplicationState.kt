import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import com.highlightEditor.editor.diagnostics.TextAnalyzer
import com.highlightEditor.util.Settings
import com.highlightEditor.window.EditorWindowState
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
    val settings = Settings()
    val tray = TrayState()

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

    fun sendNotification(notification: Notification) {
        tray.sendNotification(notification)
    }

    suspend fun exit() {
        val windowsCopy = windows.reversed()
        for (window in windowsCopy) {
            if (!window.exit()) {
                break
            }
        }
    }
}