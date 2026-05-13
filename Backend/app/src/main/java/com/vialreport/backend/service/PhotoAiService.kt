package com.vialreport.backend.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.Base64

class PhotoAiService(private val apiKey: String) {

    private val log = LoggerFactory.getLogger(PhotoAiService::class.java)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun isRoadIncident(imageBytes: ByteArray, mimeType: String): Boolean {
        if (apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY not set — skipping AI photo filter")
            return true
        }

        val b64 = Base64.getEncoder().encodeToString(imageBytes)

        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(inlineData = InlineData(mimeType = mimeType, data = b64)),
                        GeminiPart(text = "Does this image show a real road or public infrastructure incident (pothole, damaged sign, flooded road, broken traffic light, cracked sidewalk, fallen debris)? Reply with exactly one word: YES or NO.")
                    )
                )
            )
        )

        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            val response: GeminiResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val answer = response.candidates
                .firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?.trim()?.uppercase() ?: "NO"
            log.info("AI photo verdict: $answer")
            answer.contains("YES")
        } catch (e: Exception) {
            log.error("AI photo check failed: ${e.message} — allowing upload")
            true
        }
    }
}

// ── Gemini API DTOs (internal) ───────────────────────────────────

@Serializable
private data class GeminiRequest(val contents: List<GeminiContent>)

@Serializable
private data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
private data class GeminiPart(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
private data class InlineData(
    val mimeType: String,
    val data: String
)

@Serializable
private data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList())

@Serializable
private data class GeminiCandidate(val content: GeminiContent = GeminiContent(emptyList()))
