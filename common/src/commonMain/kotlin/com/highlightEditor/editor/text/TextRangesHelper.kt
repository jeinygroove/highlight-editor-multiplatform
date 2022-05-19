package com.highlightEditor.editor.text

import androidx.compose.ui.unit.IntOffset

/**
 * Contains all useful methods for translating text ranges to layout positions and vice versa
 */
interface TextRangesHelper {
    fun getPositionForTextRange(range: IntRange): Position?
    fun getOffsetForPosition(position: IntOffset): Int
}