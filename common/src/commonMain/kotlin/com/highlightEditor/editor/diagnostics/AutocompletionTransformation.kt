package com.highlightEditor.editor.diagnostics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class AutocompletionTransformation(private val autocomplete: String): VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return offset
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset >= text.length) return text.length
                return offset
            }
        }

        val newSpanStyle = text.spanStyles.toMutableList()
        if (autocomplete.isNotEmpty()) {
            newSpanStyle.add(
                AnnotatedString.Range(
                    text.spanStyles.last().item.copy(color = Color.Gray),
                    text.text.length,
                    text.text.length + autocomplete.length
                )
            )
        }
        return TransformedText(AnnotatedString(
            text = text.text + autocomplete,
            spanStyles = newSpanStyle,
        ), offsetMapping)
    }
}