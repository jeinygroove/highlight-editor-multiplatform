package com.highlightEditor.util

import com.highlightEditor.editor.text.Sentence

actual object SentenceTokenizer {
    actual fun tokenizeText(text: String): MutableList<Sentence> {
        return mutableListOf(Sentence(text, IntRange(0, text.length)))
    }
}