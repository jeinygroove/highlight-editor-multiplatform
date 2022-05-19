package com.highlightEditor.editor.text

import androidx.compose.ui.geometry.Rect

/* Used for instances which are located in multiple lines */
typealias Position = List<TextSegment>

/**
 * Represents segment of text in terms of text field layout.
 *
 * @property line line in layout (enumeration from 0)
 * @property boundingRect bounding rect of segment
 */
class TextSegment(
    val line: Int,
    val boundingRect: Rect
) {
    fun copy(y: Float) = TextSegment(this.line, this.boundingRect.translate(translateX = 0f, translateY = y))
}