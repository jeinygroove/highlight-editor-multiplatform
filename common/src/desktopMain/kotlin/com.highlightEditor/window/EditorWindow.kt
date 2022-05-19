package com.highlightEditor.window

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.*
import com.highlightEditor.editor.CodeEditor
import com.highlightEditor.editor.diagnostics.DiagnosticPopup
import com.highlightEditor.editor.docTree.DocumentType
import com.highlightEditor.editor.text.OffsetState
import com.highlightEditor.util.FileDialog
import com.highlightEditor.util.YesNoCancelDialog
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.Flow
import java.awt.Desktop
import java.net.URI
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorWindow(state: EditorWindowState) {
    val scope = rememberCoroutineScope()

    fun exit() = scope.launch { state.exit() }

    Window(
        state = state.window,
        title = titleOf(state),
        onCloseRequest = { exit() }
    ) {
        LaunchedEffect(Unit) { state.run() }

        val offsetTop = mutableStateOf<Float>(0f)
        WindowNotifications(state)
        WindowMenuBar(state)
        DiagnoseWindow(state)

        LaunchedEffect(state.editorState.textState.text.text) {
            state.autocompleteText()
        }

        CodeEditor(
            editorState = state.editorState,
            diagnosticState = state.diagnosticState,
            diagnosticInfo = state.diagnosticInfo,
            modifier = Modifier
                .fillMaxSize().padding(30.dp)
                .pointerMoveFilter(
                    onMove = {
                        state.editorState.onCursorMove(it.round())
                        state.diagnosticState.onCursorMove(it.round().let { it -> it.copy(y = it.y + state.editorState.scrollState.value) })
                        false
                    },
                    onExit = {
                        state.editorState.onCursorMove(OffsetState.Unspecified)
                        state.diagnosticState.onCursorMove(OffsetState.Unspecified)
                        false
                    }
                ).onGloballyPositioned { it ->
                    offsetTop.value = it.boundsInWindow().top },
            enabled = state.isInit,
            onTextChange = { v, type ->
                state.diagnosticState.updateAutocomplete(v.text)
                state.setText(v, type)
            }
        )

        if (state.editorState.cursorPointsElement.value?.type == DocumentType.LINK) {
            Popup(
                offset = (state.editorState.textState.textLayoutResult?.getCursorRect(state.editorState.textState.getOffsetForPosition(state.editorState.cursorPosition.value))?.bottomLeft?.round()
                    ?: IntOffset.Zero).let { it -> it.copy(y = it.y + 70 + with(LocalDensity.current) { 30.dp.toPx() }.roundToInt(), x = it.x + 60) }
            ) {
                Box(Modifier.border(Dp.Hairline, Color.Gray, shape = RoundedCornerShape(10)).background(color = Color.White)) {
                    Text(
                        text = "Open link in a browser",
                        modifier = Modifier.clickable {
                            val uri = URI(state.editorState.cursorPointsElement.value!!.value)
                            val osName by lazy(LazyThreadSafetyMode.NONE) {
                                System.getProperty("os.name").lowercase(Locale.getDefault())
                            }
                            val desktop = Desktop.getDesktop()
                            when {
                                Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE) -> desktop.browse(
                                    uri
                                )
                                "mac" in osName -> Runtime.getRuntime().exec("open $uri")
                                "nix" in osName || "nux" in osName -> Runtime.getRuntime().exec("xdg-open $uri")
                                else -> throw RuntimeException("cannot open $uri")
                            }
                        }.padding(8.dp)
                    )
                }
            }
        }

        if (state.openDialog.isAwaiting) {
            FileDialog(
                title = "Notepad",
                isLoad = true,
                onResult = {
                    state.openDialog.onResult(it)
                }
            )
        }

        if (state.saveDialog.isAwaiting) {
            FileDialog(
                title = "Notepad",
                isLoad = false,
                onResult = { state.saveDialog.onResult(it) },
                extension = state.extension
            )
        }

        if (state.exitDialog.isAwaiting) {
            YesNoCancelDialog(
                title = "Notepad",
                message = "Save changes?",
                onResult = { state.exitDialog.onResult(it) }
            )
        }
    }
}

private fun titleOf(state: EditorWindowState): String {
    val changeMark = if (state.isChanged) "*" else ""
    println("STATE PATH ${state.path}")
    val filePath = state.path ?: "Untitled.dm"
    return "$changeMark$filePath - Notepad"
}

@Composable
private fun WindowNotifications(state: EditorWindowState) {
    // Usually we take into account something like LocalLocale.current here
    fun EditorWindowNotification.format() = when (this) {
        is EditorWindowNotification.SaveSuccess -> Notification(
            "File is saved", path.toString(), Notification.Type.Info
        )
        is EditorWindowNotification.SaveError -> Notification(
            "File isn't saved", path.toString(), Notification.Type.Error
        )
    }

    LaunchedEffect(Unit) {
        state.notifications.collect {
            state.sendNotification(it.format())
        }
    }
}

@Composable
private fun FrameWindowScope.WindowMenuBar(state: EditorWindowState) = MenuBar {
    val scope = rememberCoroutineScope()
    val diagnosticScope = rememberCoroutineScope()

    fun save() = scope.launch {
        state.saveAsDM()
        state.save() }
    fun open() = scope.launch { state.open() }
    fun exit() = scope.launch { state.exit() }
    fun exportMarkdown() = scope.launch {
        state.exportMarkdown()
        state.save()
    }
    fun exportLaTeX() = scope.launch {
        state.exportLaTeX()
        state.save()
    }

    Menu("File") {
        Item("New window", onClick = {
            state.newWindow(scope, diagnosticScope)
        })
        Item("Open...", onClick = { open() })
        Menu("Export to") {
            Item("Markdown", onClick = { exportMarkdown() })
            Item("LaTeX", onClick = { exportLaTeX() })
        }
        Item("Save", onClick = { save() }, enabled = state.isChanged || state.path == null)
        Separator()
        Item("Exit", onClick = { exit() })
    }

    Menu("Settings") {
        Item(
            if (state.settings.isTrayEnabled) "Hide tray" else "Show tray",
            onClick = state.settings::toggleTray
        )
        Item(
            if (state.window.placement == WindowPlacement.Fullscreen) "Exit fullscreen" else "Enter fullscreen",
            onClick = state::toggleFullscreen
        )
    }
}