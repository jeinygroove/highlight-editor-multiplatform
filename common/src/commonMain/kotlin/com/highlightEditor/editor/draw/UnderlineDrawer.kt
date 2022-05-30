package com.highlightEditor.editor.draw

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.highlightEditor.editor.text.Position

class UnderlineDrawer(
    private val backgroundColor: Color? = null,
    private val borderColor: Color? = null,
    zIndex: Int
) : HighlightDrawer(zIndex) {
    private val paintLine = backgroundColor?.let {
        Paint().apply {
            color = backgroundColor.copy(alpha = 0.5f)
            isAntiAlias = false
            strokeWidth = 5f
            filterQuality = FilterQuality.None
        }
    }

    private val paintLineFocused = backgroundColor?.let {
        Paint().apply {
            color = backgroundColor
            isAntiAlias = false
            strokeWidth = 5f
            filterQuality = FilterQuality.None
        }
    }

    private val paintBorder = borderColor?.let {
        Paint().apply {
            color = borderColor
            style = PaintingStyle.Stroke
            strokeWidth = 1f
            isAntiAlias = false
            filterQuality = FilterQuality.None
        }
    }

    fun drawWithFocus(position: Position, drawScope: DrawScope, isFocused: Boolean = false)  {
        val paint: Paint? = if (isFocused) paintLineFocused else paintLine
        val left = drawScope.center.x - drawScope.size.width / 2
        val right = left + drawScope.size.width
        val top = drawScope.center.y - drawScope.size.height / 2
        val bottom = top + drawScope.size.height;

        drawScope.drawIntoCanvas { canvas ->
            position.forEach { segment ->
                if (segment.boundingRect.top in top..bottom || segment.boundingRect.bottom in top..bottom) {
                    paint?.let {
                        canvas.drawLine(segment.boundingRect.bottomLeft, segment.boundingRect.bottomRight, paint)
                        //canvas.drawRect(segment.boundingRect, paintFill)
                    }

                    paintBorder?.let {
                        segment.boundingRect.inflate(strokeShift)
                        canvas.drawRect(
                            segment.boundingRect.inflate(strokeShift),
                            paintBorder
                        )
                    }
                }
            }
        }
    }

    override fun draw(position: Position, drawScope: DrawScope) {
        drawWithFocus(position, drawScope)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        other as UnderlineDrawer

        if (backgroundColor != other.backgroundColor) return false
        if (borderColor != other.borderColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = backgroundColor?.hashCode() ?: 0
        result = 31 * result + (borderColor?.hashCode() ?: 0)
        return result
    }

    companion object {
        private const val strokeShift = .5f
    }
}