package com.highlightEditor.util

import androidx.compose.ui.geometry.Offset
import com.highlightEditor.editor.text.Sentence

data class Rule(val regex: Regex)

expect object SentenceTokenizer {
    fun tokenizeText(text: String): MutableList<Sentence>
}