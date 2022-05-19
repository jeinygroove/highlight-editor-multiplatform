import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.MenuScope
import androidx.compose.ui.window.Tray
import com.highlightEditor.util.LocalAppResources
import com.highlightEditor.window.EditorWindow
import kotlinx.coroutines.launch

@Composable
fun ApplicationScope.EditorApplication(state: EditorApplicationState) {
    if (state.settings.isTrayEnabled && state.windows.isNotEmpty()) {
        ApplicationTray(state)
    }

    for (window in state.windows) {
        key(window) {
            EditorWindow(window)
        }
    }
}

@Composable
private fun ApplicationScope.ApplicationTray(state: EditorApplicationState) {
    Tray(
        LocalAppResources.current.icon,
        state = state.tray,
        hint = "Editor",
        menu = { ApplicationMenu(state) }
    )
}

@Composable
private fun MenuScope.ApplicationMenu(state: EditorApplicationState) {
    val scope = rememberCoroutineScope()
    val diagnosticScope = rememberCoroutineScope()
    fun exit() = scope.launch { state.exit() }

    Item("New", onClick = { state.newWindow(scope, diagnosticScope) })
    Separator()
    Item("Exit", onClick = { exit() })
}