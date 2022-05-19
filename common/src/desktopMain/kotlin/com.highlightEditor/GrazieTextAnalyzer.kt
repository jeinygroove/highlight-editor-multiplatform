package com.highlightEditor

import ai.grazie.client.common.GrazieOkHTTPClient
import ai.grazie.gec.cloud.CloudGECAggregatedCorrection
import ai.grazie.nlp.langs.Language
import com.highlightEditor.editor.diagnostics.DiagnosticElement
import com.highlightEditor.editor.diagnostics.TextAnalyzer
import com.intellij.grazie.client.common.GrazieHTTPClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*
import io.ktor.serialization.gson.*
import java.awt.SystemColor.text

data class CompletionRequest(
    val context: String,
    val prefix: String,
    val min_len: Int = 1,
    val max_len: Int = 5,
    val num_seqs: Int = 3,
    val min_avg_log_prob: Double = 0.0,
    val min_prob: Double = 0.0,
    val min_addition_len: Int = 6,
    val rank_score_th: Int = -10
)

data class CompletionResponse(
    val completions: List<String>
)


class GrazieTextAnalyzer: TextAnalyzer {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJHcmF6aWUgQXV0aGVudGljYXRpb24iLCJhY2Nlc3MiOiJBTEwiLCJzdGFnZSI6IjIwMjEtMjAyMiIsInRva2VuX2lkIjoyODk5ODU0NTc3NDEwMTcwMzcsInNlcnZpY2UiOiJJbnRlcm5zaGlwIiwiZXhwIjoxNjU3ODg4ODg0fQ.lsf12p1vXyEVcWde_zvT3NKrQl9NHJZ-su0fhsjWZjbteJOfs3xHZ0RbFLRyCMTgmMRTN27TDU5Sr6kgt0USFsRmVwXrUAAwD1QqSEYvMoTEQ05PUnfNZ1X_AjqWh2CleuHBJEq-jeEF5V-E2CcEb_9CrcFhIHa-AxO4rrL2XOdwXISLBea-dYp6yoht8u7RFvX9sD-eUTf9EO8Wo20yAQm1IGG0sz0o2EZK-MpzTrygPCmAuivSEd2X8OxTTrFmmliYRSdaS0IBiO5Gs6QovJPia4b1d8C_Faru5wSQTPkCdsvjpPQlD7kwqtD7HH0F4aSnzTLhZR-GO-aRBmaMQQ"

    private var correction: CloudGECAggregatedCorrection
    private val completionClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    init {
        correction = CloudGECAggregatedCorrection(
            language = Language.ENGLISH,
            remote = CloudGECAggregatedCorrection.Remote(
                server = "https://all.agg.gec.stgn.grazie.aws.intellij.net/",
                token = token,
                type = GrazieHTTPClient.WithV3Auth.RequestType.Service
            ),
            client = GrazieOkHTTPClient
        )
    }

    override suspend fun analyze(text: String): List<DiagnosticElement> {
        val res = correction.correct(listOf(text)).getOrNull(0) ?: return listOf()
        val diagnosticElements = res.corrections.toList().map { it ->
            DiagnosticElement(
                offset = it.errorRange.start,
                length = it.errorRange.length,
                message = it.message,
                suggestions = it.replacements.toList()
            )
        }
        println("DIAG")
        return diagnosticElements
    }

    override suspend fun autocomplete(context: String, prefix: String): List<String> {
        val response: CompletionResponse = completionClient.post("https://en.nlc.trf.stgn.grazie.aws.intellij.net/service/v3/complete") {
            contentType(ContentType.Application.Json)
            header("Grazie-Authenticate-JWT", token)
            setBody(CompletionRequest(context, prefix))
        }.body()

        return response.completions.map { it -> it.substring(prefix.length) }
    }
}