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

/** Resultado de la validación: approved=true si se acepta, reason siempre disponible para logs/UI */
data class ValidationResult(val approved: Boolean, val reason: String)

class PhotoAiService(private val apiKey: String) {

    private val log = LoggerFactory.getLogger(PhotoAiService::class.java)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun validate(imageBytes: ByteArray, mimeType: String): ValidationResult {
        if (apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY no configurado — omitiendo validación de IA")
            return ValidationResult(approved = true, reason = "Sin validación (API key no configurada)")
        }

        val b64 = Base64.getEncoder().encodeToString(imageBytes)

        val prompt = """
            Look at this image and answer with exactly one word: YES or NO.

            Answer NO if ANY of these is true:
            - The image is a cartoon, anime, drawing, illustration, meme, clip art, CGI, painting, or any non-photographic content.
            - The image contains nudity, sexual content, or graphic violence.
            - The image is a real photo but shows ONLY: food, a selfie, an animal, or an indoor room with no street visible.

            Answer YES only if BOTH are true:
            - It is a real photograph taken with a camera (not drawn or animated).
            - It shows any outdoor public area: street, road, sidewalk, pothole, flooding, garbage pile, broken sign, traffic light, landslide, pavement crack, or streetlight.

            Reply with a single word: YES or NO
        """.trimIndent()

        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(inlineData = InlineData(mimeType = mimeType, data = b64)),
                        GeminiPart(text = prompt)
                    )
                )
            ),
            generationConfig = GenerationConfig(maxOutputTokens = 5)
        )

        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$apiKey"
            val response: GeminiResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val candidate = response.candidates.firstOrNull()
            val raw = candidate?.content?.parts?.firstOrNull()?.text?.trim()?.uppercase() ?: ""
            val finishReason = candidate?.finishReason ?: "UNKNOWN"

            log.info("Gemini raw='$raw' finishReason='$finishReason'")

            when {
                raw.startsWith("YES") ->
                    ValidationResult(true,  "Aceptada por Gemini")
                raw.startsWith("NO") ->
                    ValidationResult(false, "Gemini: imagen no válida (respuesta: $raw)")
                finishReason == "SAFETY" ->
                    ValidationResult(false, "Gemini bloqueó la imagen por filtro de seguridad")
                raw.isEmpty() ->
                    ValidationResult(false, "Gemini no devolvió respuesta (finishReason: $finishReason)")
                else ->
                    ValidationResult(false, "Gemini: respuesta inesperada '$raw'")
            }
        } catch (e: Exception) {
            log.error("Gemini API error: ${e.message}")
            ValidationResult(false, "Error al contactar Gemini: ${e.message}")
        }
    }
}

// ── Gemini API DTOs (internal) ───────────────────────────────────

@Serializable
private data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
private data class GenerationConfig(val maxOutputTokens: Int)

@Serializable
private data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
private data class GeminiPart(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
private data class InlineData(val mimeType: String, val data: String)

@Serializable
private data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList())

@Serializable
private data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)
