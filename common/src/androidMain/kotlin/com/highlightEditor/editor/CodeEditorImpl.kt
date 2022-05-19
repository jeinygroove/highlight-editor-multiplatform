package com.highlightEditor.editor

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.highlightEditor.fork.text.BasicTextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highlightEditor.editor.diagnostics.AutocompletionTransformation
import com.highlightEditor.editor.diagnostics.DiagnosticInfo
import com.highlightEditor.editor.diagnostics.DiagnosticState
import com.highlightEditor.editor.docTree.DocumentType
import com.highlightEditor.fork.gestures.*
import com.highlightEditor.fork.gestures.detectTapAndPress
import com.highlightEditor.fork.text.fastAll
import com.highlightEditor.fork.text.timeNowMillis
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal actual fun CodeEditorImpl(
    editorState: EditorState,
    diagnosticState: DiagnosticState,
    diagnosticInfo: DiagnosticInfo,
    modifier: Modifier,
    enabled: Boolean,
    onTextChange: (TextFieldValue, DocumentType) -> Unit
) {
    val scope = rememberCoroutineScope()

    val documentElementType = remember { mutableStateOf(DocumentType.TEXT) }
    var progress by remember { mutableStateOf(0.1f) }
    val selectedTab = remember { mutableStateOf(0) }

    LaunchedEffect(true) {
        while (true) {
            if (progress < 1) progress += 0.05f
            else progress = 0f
            delay(10)
        }
    }

    LaunchedEffect(documentElementType.value) {
        selectedTab.value = when (documentElementType.value) {
            DocumentType.TEXT -> 0
            DocumentType.HEADER -> 1
            DocumentType.LINK -> 2
            DocumentType.LIST -> 3
        }
    }

    MaterialTheme {
        Scaffold(Modifier.fillMaxSize(),
            topBar = {
                TopAppBar() {
                    TabRow(
                        selectedTabIndex = selectedTab.value,
                    ) {
                        Tab(selectedTab.value == 0, onClick = { ->
                            selectedTab.value = 0
                            editorState.textState.makeType(documentElementType.value, DocumentType.TEXT)
                            documentElementType.value = DocumentType.TEXT
                        }) {
                            Text(
                                "text",
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Tab(true, onClick = { ->
                            selectedTab.value = 1
                            editorState.textState.makeType(documentElementType.value, DocumentType.HEADER)
                            documentElementType.value = DocumentType.HEADER
                        }) {
                            Text(
                                "header",
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Tab(selectedTab.value == 2, onClick = { ->
                            selectedTab.value = 2
                            editorState.textState.makeType(documentElementType.value, DocumentType.LINK)
                            documentElementType.value = DocumentType.LINK
                        }) {
                            Text(
                                "link",
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Tab(selectedTab.value == 3, onClick = { ->
                            selectedTab.value = 3
                            editorState.textState.makeType(documentElementType.value, DocumentType.LIST)
                            documentElementType.value = DocumentType.LIST
                        }) {
                            Text(
                                "list",
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }) {
            Box {
                if (!diagnosticInfo.hasInternetConnection) {
                    Snackbar(modifier = Modifier.align(Alignment.BottomEnd).wrapContentWidth()) {
                        Text("No internet connection.")
                    }
                } else if (diagnosticInfo.diagnosticStart.let { it != null && timeNowMillis() - it >= 1000}) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                        color = MaterialTheme.colors.primary
                    )
                }

                Column(modifier) {
                    BasicTextField(
                        modifier = Modifier.fillMaxSize().drawBehind {
                            diagnosticState.diagnostics.filter { it.length != 0 }.map { el ->
                                editorState.textState.getPositionForTextRange(
                                    IntRange(el.offset, el.offset + el.length - 1)
                                )?.let {
                                    editorState.backgroundDrawer.draw(
                                        it.map { el -> el.copy(-editorState.scrollState.value.toFloat()) }, this
                                    )
                                }
                            }
                        },
                        value = editorState.textState.text,
                        onValueChange = { v ->
                            if (v.text == editorState.textState.text.text) {
                                println(v.text)
                                println(v.selection)
                                val lst = editorState.textState.documentModel.getElementsInRange(v.selection)
                                if (lst.size == 1) {
                                    documentElementType.value = lst[0].type
                                }
                            }
                            run {
                                if (v.text != editorState.textState.text.text) {
                                    editorState.textState.textLayoutResult = null
                                    diagnosticState.updateList(listOf())
                                }
                                onTextChange(v, documentElementType.value)
                            }
                        },
                        onTextLayout = { layoutRes ->
                            println(layoutRes.layoutInput.text == editorState.textState.text.annotatedString)
                            editorState.textState.textLayoutResult = layoutRes
                        },
                        onScroll = {
                            scope.launch {
                                editorState.scrollState.scrollTo(it.toInt())
                            }
                        },
                        visualTransformation = AutocompletionTransformation(diagnosticState.autocompletion.autocomplete.value),
                        textStyle = TextStyle(fontSize = 28.sp),
                        enabled = enabled
                    )
                }
            }
        }
    }
}
