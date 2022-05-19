package com.highlightEditor.fork.text

import java.text.BreakIterator

internal actual fun String.findPrecedingBreak(index: Int): Int {
    val it = BreakIterator.getCharacterInstance()
    it.setText(this)
    return it.preceding(index)
}

internal actual fun String.findFollowingBreak(index: Int): Int {
    val it = BreakIterator.getCharacterInstance()
    it.setText(this)
    return it.following(index)
}

internal actual fun StringBuilder.appendCodePointX(codePoint: Int): StringBuilder =
    this.appendCodePoint(codePoint)