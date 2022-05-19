package com.highlightEditor.editor.text

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toOffset
import com.highlightEditor.editor.docTree.DocumentElement
import com.highlightEditor.editor.docTree.DocumentModel
import com.highlightEditor.editor.docTree.DocumentType
import com.highlightEditor.util.SentenceTokenizer
import kotlin.math.abs
import kotlin.text.Typography.bullet

/**
 * Responsible for text content, positions/ranges in text field layout.
 */
class TextState(
    text: TextFieldValue
) : TextRangesHelper {
    var text by mutableStateOf(text)
    var visualTransformation by mutableStateOf<TransformedText>(TransformedText(text.annotatedString, OffsetMapping.Identity))
    var textLayoutResult by mutableStateOf<TextLayoutResult?>(null)
    private var prevSelection: TextRange = text.selection
    var documentModel: DocumentModel = DocumentModel()
    var sentences: MutableList<Sentence> = SentenceTokenizer.tokenizeText(text.text).also { println(it) }

    fun findSentenceByOffset(offset: Int): Int {
        return sentences.binarySearch { s ->
            if (s.range.contains(offset)) 0
            else if (s.range.first > offset) -1
            else 1
        }
    }

    fun updateText(newTextFieldValue: TextFieldValue, type: DocumentType = DocumentType.TEXT, changeRange: IntRange, textFix: String): IntRange? {
        documentModel.removeRange(changeRange)
        println(changeRange)
        println(documentModel.elements)
        val newChangeIntRange = IntRange(changeRange.first, changeRange.first + textFix.length - 1)
        println(newChangeIntRange)
        documentModel.addElement(DocumentElement(type, textFix, newChangeIntRange))
        text = newTextFieldValue.copy(annotatedString = documentModel.getContent())
        sentences = SentenceTokenizer.tokenizeText(text.text).also { println(it) }
        return newChangeIntRange
    }

    fun updateText(newTextFieldValue: TextFieldValue, type: DocumentType = DocumentType.TEXT): IntRange? {
        var changeRange: IntRange? = null
        val newSelection = newTextFieldValue.selection
        println("new selection $newSelection")
        // means that we typed (or deleted smth)
        if (newTextFieldValue.text != text.text) {
            println("check ${prevSelection.length} $prevSelection ${newSelection}")
            if (prevSelection.length != 0 && newSelection.length == 0) {
                documentModel.removeRange(IntRange(prevSelection.min, prevSelection.max - 1))
                val newRange = IntRange(prevSelection.min, newSelection.min - 1)
                documentModel.addElement(DocumentElement(type, newTextFieldValue.text.substring(newRange), newRange))
            } else {
            val diffInLengths = newTextFieldValue.text.length - text.text.length
            println("diff $diffInLengths")
            if (diffInLengths < 0) {
                changeRange = IntRange(newSelection.min, newSelection.min + abs(diffInLengths) - 1)
                documentModel.removeRange(changeRange)
            } else {
                changeRange = IntRange(text.selection.min, text.selection.min + abs(diffInLengths) - 1)
                println("changerange $changeRange ${documentModel.elements}")
                documentModel.addElement(DocumentElement(type, newTextFieldValue.text.substring(changeRange), changeRange))
            }
            }
        }
        prevSelection = newSelection
        text = newTextFieldValue.copy(annotatedString = documentModel.getContent())
        sentences = SentenceTokenizer.tokenizeText(text.text).also { println(it) }
        return changeRange
    }

    private fun makeList() {
        val newLineOffsetStart = text.text.subSequence(0, text.selection.min).indexOfLast { it == '\n' }
        val newLineOffsetEnd = text.text.substring(text.selection.max).indexOfFirst { it == '\n' }.let {
            if (it == -1) {
                text.text.length - 1
            } else {
                it + text.selection.max - 1
            }
        }
        val changeRange = IntRange(newLineOffsetStart + 1, newLineOffsetEnd)
        val bullet = "\t$bullet "

        if (changeRange.last - changeRange.first <= 0) {
            documentModel.addElement(DocumentElement(DocumentType.LIST, bullet, IntRange(changeRange.first, changeRange.first + bullet.length-1)))
            text = text.copy(annotatedString = documentModel.getContent(), selection = TextRange(text.selection.start + bullet.length, text.selection.end + bullet.length))
            return
        }
        val newText = bullet + text.text.substring(changeRange)
        documentModel.removeRange(changeRange)
        documentModel.addElement(DocumentElement(DocumentType.LIST, newText, IntRange(changeRange.first, changeRange.first + newText.length-1)))
        text = text.copy(annotatedString = documentModel.getContent(), selection = TextRange(text.selection.start + bullet.length, text.selection.end + bullet.length))
    }

    fun makeType(prevDocumentType: DocumentType, documentType: DocumentType) {
        if (documentType == DocumentType.LIST && prevDocumentType != DocumentType.LIST) return makeList()

        if (prevDocumentType == DocumentType.HEADER || documentType == DocumentType.HEADER) {
            if (text.text.isEmpty()) return
            println("CALL")
            println(text.selection)
            val newLineOffsetStart = text.text.subSequence(0, text.selection.min).indexOfLast { it == '\n' }
            val newLineOffsetEnd = text.text.substring(text.selection.max).indexOfFirst { it == '\n' }.let {
                if (it == -1) {
                    text.text.length - 1
                } else {
                    it + text.selection.max - 1
                }
            }
            val changeRange = IntRange(newLineOffsetStart + 1, newLineOffsetEnd)
            println("kek")
            println(changeRange)
            if (changeRange.last - changeRange.first <= 0) return
            documentModel.removeRange(changeRange)
            val textHeaders = text.text.substring(changeRange).split('\n')
            var offset = newLineOffsetStart + 1
            for (i in textHeaders.indices) {
                val header = textHeaders[i]
                if (i != textHeaders.size-1) {
                    documentModel.addElement(
                        DocumentElement(
                            documentType,
                            header + '\n',
                            IntRange(offset, offset + header.length)
                        )
                    )
                    offset += header.length + 1
                } else {
                    documentModel.addElement(
                        DocumentElement(
                            documentType,
                            header,
                            IntRange(offset, offset + header.length - 1)
                        )
                    )
                }
            }
            println(documentModel.elements)
            text = text.copy(annotatedString = documentModel.getContent())
        }
    }

    override fun getPositionForTextRange(range: IntRange): Position? {
        return textLayoutResult?.let { textLayoutResult ->
            println(textLayoutResult.layoutInput.text.text)
            val startLine = textLayoutResult.getLineForOffset(range.first)
            val endLine = textLayoutResult.getLineForOffset(range.last)
            val startCharacterTopLeft = textLayoutResult.getBoundingBox(range.first).topLeft
            val endCharacterBottomRight = textLayoutResult.getBoundingBox(range.last).bottomRight

            println(startLine)
            return if (startLine == endLine) {
                listOf(TextSegment(startLine, Rect(startCharacterTopLeft, endCharacterBottomRight)))
            } else {
                val list = mutableListOf<TextSegment>()
                list.add(
                    TextSegment(
                        startLine,
                        Rect(
                            startCharacterTopLeft,
                            Offset(textLayoutResult.getLineRight(startLine), textLayoutResult.getLineBottom(startLine))
                        )
                    )
                )
                var line = startLine + 1
                while (line < endLine) {
                    val lineBoundingBox = Rect(
                        Offset(
                            textLayoutResult.getLineLeft(line),
                            textLayoutResult.getLineTop(line)
                        ),
                        Offset(
                            textLayoutResult.getLineRight(line),
                            textLayoutResult.getLineBottom(line)
                        )
                    )
                    list.add(
                        TextSegment(
                            line,
                            lineBoundingBox
                        )
                    )
                    line++
                }
                list.add(
                    TextSegment(
                        endLine,
                        Rect(
                            Offset(textLayoutResult.getLineLeft(endLine), textLayoutResult.getLineTop(endLine)),
                            endCharacterBottomRight
                        )
                    )
                )
                list
            }
        }
    }

    override fun getOffsetForPosition(cursorPosition: IntOffset): Int {
        return textLayoutResult?.let { layoutResult ->
            if (cursorPosition.x < 0 || cursorPosition.y < 0 || cursorPosition.y > layoutResult.size.height) {
                return -1
            }
            val offsetForPosition = layoutResult.getOffsetForPosition(cursorPosition.toOffset())
            val lineIndex = layoutResult.getLineForOffset(offsetForPosition)
            if (cursorPosition.x > layoutResult.getLineRight(lineIndex)
                || cursorPosition.y > layoutResult.getLineBottom(lineIndex)
                || cursorPosition.y < layoutResult.getLineTop(lineIndex)
            ) {
                return -1
            }
            return offsetForPosition
        } ?: -1
    }

    fun getElementForOffset(offset: Int): DocumentElement? {
        val invIndex = documentModel.getElementByOffset(offset)
        return if (invIndex >= 0)
            documentModel.elements[invIndex]
        else null
    }
}