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

    /**
     * Valida que la imagen sea una fotografía real de un incidente vial registrado en VialReport.
     * Retorna true = aceptada, false = rechazada.
     * El motivo de rechazo queda registrado en el log.
     */
    suspend fun isRoadIncident(imageBytes: ByteArray, mimeType: String): Boolean {
        if (apiKey.isBlank()) {
            log.error("GEMINI_API_KEY no configurado — validación de IA desactivada. Configura la variable en Render.")
            return true  // sin clave: se acepta sin validación
        }

        val b64 = Base64.getEncoder().encodeToString(imageBytes)

        val prompt = """
            You are a photo validator for a road incident reporting app.

            Answer YES if the image is a real photograph (not a cartoon or drawing) AND shows ANY of:
            roads, streets, sidewalks, potholes, flooding, garbage on street, broken signs,
            broken traffic lights, landslides, cracks in pavement, or broken streetlights.
            Accept photos taken at night, from a car window, or with imperfect framing.

            Answer NO only if the image is:
            - A cartoon, illustration, meme, drawing, or AI-generated image
            - Clearly obscene or contains nudity/graphic violence
            - Clearly unrelated (food, selfie, animal, indoor scene with no street visible)

            Reply with a single word: YES or NO.
        """.trimIndent()

        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(inlineData = InlineData(mimeType = mimeType, data = b64)),
                        GeminiPart(text = prompt)
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

            val raw = response.candidates
                .firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?.trim()?.uppercase() ?: ""

            log.info("Gemini raw response: '$raw'")

            // Acepta si la respuesta contiene YES en cualquier parte
            // Rechaza solo si contiene NO de forma explícita y sin YES
            val accepted = when {
                raw.contains("YES") -> { log.info("Gemini: ACEPTADA");  true  }
                raw.contains("NO")  -> { log.warn("Gemini: RECHAZADA"); false }
                else                -> { log.warn("Gemini: respuesta ambigua '$raw' — aceptando"); true }
            }
            accepted
        } catch (e: Exception) {
            log.error("Gemini API falló: ${e.message} — rechazando imagen por precaución")
            false  // fail-closed
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
