package com.highlightEditor.editor.draw

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.highlightEditor.editor.text.Position

abstract class HighlightDrawer(
    private val zIndex: Int
) : Comparable<HighlightDrawer> {

    abstract fun draw(position: Position, drawScope: DrawScope)

    override fun compareTo(other: HighlightDrawer): Int {
        val comp = zIndex.compareTo(other.zIndex)
        return if (comp != 0) comp
        else this.hashCode() - other.hashCode()
    }
}