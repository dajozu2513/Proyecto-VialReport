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
            log.error("GEMINI_API_KEY no configurado — validación de IA desactivada. Configura la variable en Render.")
            return true  // sin clave: se acepta pero sin validación
        }

        val b64 = Base64.getEncoder().encodeToString(imageBytes)

        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(inlineData = InlineData(mimeType = mimeType, data = b64)),
                        GeminiPart(text = """
                            You are a road infrastructure incident validator.
                            Analyze this image and answer YES only if ALL of the following are true:
                            1. It is a REAL photograph (not a cartoon, illustration, meme, screenshot, anime, AI-generated art, or drawing).
                            2. It clearly shows an actual road, street, sidewalk, or public infrastructure.
                            3. It shows a visible problem such as: pothole, road damage, broken traffic sign, flooded road, fallen debris, damaged sidewalk, broken streetlight, cracked pavement, or similar infrastructure defect.
                            If the image is a cartoon, meme, unrelated photo, or does not show infrastructure damage, reply NO.
                            Reply with ONLY one word: YES or NO.
                        """.trimIndent())
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
            log.info("Gemini verdict: '$answer'")
            answer.startsWith("YES")
        } catch (e: Exception) {
            log.error("Gemini API call failed: ${e.message} — rejecting image as precaution")
            false  // fail-closed: si la API falla, rechaza la imagen
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
