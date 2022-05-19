import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.*
import com.highlightEditor.GrazieTextAnalyzer
import com.highlightEditor.util.LocalAppResources
import com.highlightEditor.util.rememberAppResources

fun main() = application {
    CompositionLocalProvider(LocalAppResources provides rememberAppResources()) {
        EditorApplication(rememberApplicationState(GrazieTextAnalyzer()))
    }
}