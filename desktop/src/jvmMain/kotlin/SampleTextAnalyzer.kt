import com.highlightEditor.editor.diagnostics.DiagnosticElement
import com.highlightEditor.editor.diagnostics.TextAnalyzer
import com.highlightEditor.editor.text.Sentence

import io.ktor.client.*
import kotlinx.serialization.Serializable

@Serializable
class AnalyzeResponse(
    val status: String,
    val response: AnalyzeResult
)

@Serializable
class AnalyzeResult(
    val result: String,
    val errors: List<AnalyzeError>
)

@Serializable
class AnalyzeError(
    val id: String,
    val offset: Int,
    val length: Int,
    val bad: String,
    val better: List<String>,
    val type: String
)

class SampleTextAnalyzer(private val client: HttpClient): TextAnalyzer {
    override suspend fun analyze(text: List<Sentence>): List<DiagnosticElement> {
        // TODO Don't forget to replace "KEY" with the real one
        val response: AnalyzeResponse? = try {
            AnalyzeResponse("", AnalyzeResult("kek", listOf(AnalyzeError("", 2, 2, "spell error", listOf("am", "was", "have been"), "no type"))))
            /*client.post("https://api.textgears.com/grammar?key=96j46qswOBF4Ph20&language=en-GB") {
                contentType(ContentType.Application.Json)
                body = buildJsonObject {
                    put("text", text)
                }
            }*/
        } catch (cause: Throwable) {
            null
        }

        return response?.response?.errors?.map { err ->
            DiagnosticElement(
                offset = err.offset,
                length = err.length,
                message = err.bad,
                suggestions = err.better
            )
        } ?: listOf()
    }

    override suspend fun autocomplete(context: String, prefix: String): List<String> {
        TODO("Not yet implemented")
    }
}