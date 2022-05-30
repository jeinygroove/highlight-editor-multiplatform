package me.olgashimanskaia.common

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import com.highlightEditor.editor.EditorState
import com.highlightEditor.editor.diagnostics.DiagnosticElement
import com.highlightEditor.editor.diagnostics.DiagnosticInfo
import com.highlightEditor.editor.diagnostics.DiagnosticState
import com.highlightEditor.editor.docTree.DocumentType
import com.highlightEditor.fork.text.timeNowMillis
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.nio.file.Path

class EditorWindowState(
    private val application: EditorApplicationState,
    val scope: CoroutineScope,
    val diagnosticScope: CoroutineScope,
    path: Path?,
    private val exit: (EditorWindowState) -> Unit
) {

    var path by mutableStateOf(path)
        private set

    var isChanged by mutableStateOf(false)
        private set

    var diagnosticInfo by mutableStateOf(DiagnosticInfo(null, true))
    var diagnosticInProcess by mutableStateOf(false)
        private set

    var autocompletionInProcess by mutableStateOf(false)
        private set


    private var _notifications = Channel<EditorWindowNotification>(0)
    val notifications: Flow<EditorWindowNotification> get() = _notifications.receiveAsFlow()

    private val _editorState by mutableStateOf(EditorState(TextFieldValue(""), scope))
    private val _diagnosticState by mutableStateOf(DiagnosticState(_editorState.textState, diagnosticScope))

    val editorState: EditorState
        get() = _editorState
    val diagnosticState: DiagnosticState
        get() = _diagnosticState

    fun setText(value: TextFieldValue, type: DocumentType): IntRange? {
        check(isInit)
        isChanged = true
        return _editorState.textState.updateText(value, type)
    }

    fun setText(value: TextFieldValue, type: DocumentType, changeRange: IntRange, textFix: String): IntRange? {
        check(isInit)
        isChanged = true
        return _editorState.textState.updateText(value, type, changeRange, textFix)
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun autocompleteText() {
        val text = _editorState.textState.text.text
        val prefix = text.split(' ').last()

        val matched = _diagnosticState.updateAutocomplete(text)
        println("Matched ${matched}")
        if (matched) return
        GlobalScope.launch {
            if (!autocompletionInProcess) {
                //autocompletionInProcess = true
                var autocomplete: String? = null
                try {
                    autocomplete = application.analyzer.autocomplete(text, prefix).getOrNull(0)
                    diagnosticInfo.hasInternetConnection = true
                } catch (_: Exception) {
                    diagnosticInfo.hasInternetConnection = false
                    autocompletionInProcess = false
                }

                if (autocomplete != null && _editorState.textState.text.text == text) {
                    _diagnosticState.setAutocomplete(autocomplete, text)
                }
                autocompletionInProcess = false
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun diagnoseText() {
        GlobalScope.launch {
            if (!diagnosticInProcess) {
                diagnosticInProcess = true
                diagnosticInfo.diagnosticStart = timeNowMillis()
                val text = _editorState.textState.text.text
                val sentences = _editorState.textState.sentences
                var diagnostic: List<DiagnosticElement> = listOf()
                try {
                    diagnostic = application.analyzer.analyze(sentences)
                    diagnosticInfo.hasInternetConnection = true
                } catch (e: Exception) {
                    println(e.message)
                    diagnosticInfo.hasInternetConnection = false
                    diagnosticInProcess = false
                    diagnosticInfo.diagnosticStart = null
                }

                // TODO split by sentences
                println(diagnostic)
                println("HERE")
                println(text)
                println(_editorState.textState.text.text)
                diagnostic = diagnostic.filter { elem ->
                    elem.offset + elem.length < _editorState.textState.text.text.length && _editorState.textState.text.text.subSequence(elem.offset, elem.offset + elem.length) == text.subSequence(elem.offset, elem.offset + elem.length)
                }
                diagnosticState.updateList(diagnostic)
                // TODO do smth so it won't affect typing
                diagnosticInProcess = false
                diagnosticInfo.diagnosticStart = null
            }
        }
    }


    private suspend fun _setText(value: TextFieldValue) {
        val changeRange = _editorState.textState.updateText(value)
        val diagnostic = application.analyzer.analyze(_editorState.textState.sentences)
        diagnosticState.updateList(diagnostic)
    }

    var isInit by mutableStateOf(false)
        private set


    suspend fun run() {
        if (path != null) {
            open(path!!)
        } else {
            initNew()
        }
    }

    private suspend fun open(path: Path) {
        isInit = false
        isChanged = false
        this.path = path
        try {
           // _setText(TextFieldValue(path.readTextAsync()))
            isInit = true
        } catch (e: Exception) {
            e.printStackTrace()
            _editorState.textState.updateText(TextFieldValue("Cannot read $path"))
            _diagnosticState.updateList(listOf())
        }
    }

    private fun initNew() {
        _editorState.textState.updateText(TextFieldValue(""))//AnnotatedString("I is an apple", listOf(AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold, color = Color.Red), 0, 8)))))
        _diagnosticState.updateList(listOf())
        isInit = true
        isChanged = false
    }

    private var saveJob: Job? = null
}

sealed class EditorWindowNotification {
    class SaveSuccess(val path: Path) : EditorWindowNotification()
    class SaveError(val path: Path) : EditorWindowNotification()
}

class DialogState<T> {
    private var onResult: CompletableDeferred<T>? by mutableStateOf(null)

    val isAwaiting get() = onResult != null

    suspend fun awaitResult(): T {
        onResult = CompletableDeferred()
        val result = onResult!!.await()
        onResult = null
        return result
    }

    fun onResult(result: T) = onResult!!.complete(result)
}