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
            newText = newText.replace(rule.regex, "\uE001")
        }

        val sentencesContent = newText.split(Regex("\uE001"))
        val sentences = mutableListOf<Sentence>()
        var indx = 0
        for (s in sentencesContent) {
            sentences.add(Sentence(s, IntRange(indx, indx + s.length)))
            indx += s.length + 1
        }
        return sentences
    }

    fun cleanXml(s: String): String {
        return s.replace(Regex("""<!--[\s\S]*?-->"""), "").replace(Regex(""">\s+<"""), "><")
    }

    fun decode(s: String): String {
        return s.replace("&lt;", "<").replace("&rt;", ">")
    }
}