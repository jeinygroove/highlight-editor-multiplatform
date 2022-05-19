package me.olgashimanskaia.common

import android.content.Intent
import android.net.Uri
import android.util.Half.round
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup
import androidx.core.content.ContextCompat.startActivity
import com.highlightEditor.editor.CodeEditorImpl
import com.highlightEditor.editor.docTree.DocumentType
import com.highlightEditor.fork.gestures.awaitFirstDown
import com.highlightEditor.fork.gestures.awaitSecondDown
import com.highlightEditor.fork.gestures.detectTapGestures
import kotlinx.coroutines.coroutineScope
import kotlin.math.roundToInt

@Composable
fun App(applicationState: EditorApplicationState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state = applicationState.windows[0]

    LaunchedEffect(Unit) { state.run() }

    DiagnoseWindow(state)

    LaunchedEffect(state.editorState.textState.text.text) {
        state.autocompleteText()
    }

    CodeEditorImpl(
            editorState = state.editorState,
            diagnosticState = state.diagnosticState,
            diagnosticInfo = state.diagnosticInfo,
            modifier = Modifier
                .fillMaxSize().padding(30.dp)
                .pointerInput(true) {
                    forEachGesture {
                        coroutineScope {
                            awaitPointerEventScope {
                                val event = awaitFirstDown(false)
                                val event2 = awaitSecondDown(event, false)
                                println("$event, $event2")
                                if (event2 != null) {
                                    println("DOUBLE $event2")
                                    state.editorState.onCursorMove(event2.position.round())
                                    state.diagnosticState.onCursorMove(
                                        event2.position.round().let { it -> it.copy(y = it.y + state.editorState.scrollState.value) })
                                }
                            }
                        }
                    }
                },
            enabled = state.isInit,
            onTextChange = { v, type ->
                state.diagnosticState.updateAutocomplete(v.text)
                state.setText(v, type)
            }
        )

        if (state.editorState.cursorPointsElement.value?.type == DocumentType.LINK) {
            Popup(
                offset = (state.editorState.textState.textLayoutResult?.getCursorRect(state.editorState.textState.getOffsetForPosition(state.editorState.cursorPosition.value))?.bottomLeft?.round()
                    ?: IntOffset.Zero).let { it -> it.copy(y = it.y + 120 + with(LocalDensity.current) { 30.dp.toPx() }.roundToInt(), x = it.x + 60) }
            ) {
                Box(Modifier.border(1.dp, Color.Gray, RoundedCornerShape(10)).background(color = Color.LightGray)) {
                    Text(
                        text = "Open link in a browser",
                        modifier = Modifier.clickable {
                            val link = state.editorState.cursorPointsElement.value?.value
                            if (link != null) {
                                val browserIntent: Intent = Intent(Intent.ACTION_VIEW)
                                browserIntent.data = Uri.parse(link)
                                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(browserIntent)
                            }
                        }.padding(5.dp)
                    )
                }
            }
        }

    }