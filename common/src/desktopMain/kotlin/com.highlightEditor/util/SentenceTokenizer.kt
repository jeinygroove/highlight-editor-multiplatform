package com.highlightEditor.util

import com.highlightEditor.editor.text.Sentence
import java.io.File

actual object SentenceTokenizer {
    val pattern = Regex("""<rule(?:(\s+break="no")|\s+[^>]+|\s*)>(?:<beforebreak>([^<]*)<\/beforebreak>)?(?:<afterbreak>([^<]*)<\/afterbreak>)?<\/rule>""")
    val exceptions: MutableList<Rule> = mutableListOf()
    val rules: MutableList<Rule> = mutableListOf()

    init {
        val text = File("/Users/olgashimanskaia/highlight_editor/src/desktopMain/kotlin/com/highlightEditor/util/english.srx").readText(Charsets.UTF_8)
        val cleared = this.cleanXml(text)

        val matchResults = pattern.findAll(cleared)
        for (match in matchResults) {
            val noBreak = match.groupValues.getOrNull(1)
            val before = match.groupValues.getOrNull(2)
            val after = match.groupValues.getOrNull(3)
            val regex = Regex(this.decode(before ?: "") + "(?![\uE000\uE001])" + (if (after != null) "(?=" + this.decode(after) + ")" else ""))
            if (noBreak != null && noBreak.isNotEmpty()) {
                this.exceptions.add(Rule(regex))
            } else {
                this.rules.add(Rule(regex))
            }
        }
    }

    actual fun tokenizeText(text: String): MutableList<Sentence> {
        var newText = text

        for (rule in this.rules) {
            rule.regex.findAll(newText).toList().map {
                newText = newText.replaceRange(it.range, "\uE001".repeat(it.range.step + 1))
            }
        }

        val sentences = mutableListOf<Sentence>()
        var indx = 0
        Regex("\uE001+").findAll(newText).toList().map { match ->
            val sRange = IntRange(indx, match.range.first - 1)
            sentences.add(Sentence(newText.substring(sRange), sRange))
            indx = match.range.last + 1
        }
        val sRange = IntRange(indx, newText.length-1)
        sentences.add(Sentence(newText.substring(sRange), sRange))
        return sentences
    }

    fun cleanXml(s: String): String {
        return s.replace(Regex("""<!--[\s\S]*?-->"""), "").replace(Regex(""">\s+<"""), "><")
    }

    fun decode(s: String): String {
        return s.replace("&lt;", "<").replace("&rt;", ">")
    }
}