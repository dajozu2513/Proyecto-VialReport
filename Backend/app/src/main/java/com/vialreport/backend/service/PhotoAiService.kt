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
            You are a content moderator for a road incident reporting app (VialReport, Costa Rica).

            VALID INCIDENT TYPES:
            - Flooded road or street (Inundación)
            - Broken or missing streetlight (Alumbrado público)
            - Garbage pile or illegal dumping on public road (Basura acumulada)
            - Pothole on road or sidewalk (Bache)
            - Damaged or missing road sign (Señal dañada)
            - Broken traffic light (Semáforo dañado)
            - Landslide blocking a road (Derrumbe)
            - Crack in sidewalk or pavement (Grieta en acera)

            Respond with ONLY ONE of these codes — nothing else:
            YES          → Real photo showing any of the valid incident types (even partially, from a distance, or with imperfect framing).
            NO_NOT_PHOTO → Cartoon, illustration, meme, anime, drawing, screenshot, AI art, or any non-photographic image.
            NO_OBSCENE   → Contains nudity, sexual content, graphic violence, or offensive material.
            NO_UNRELATED → Real photo but clearly shows something unrelated (food, selfie, pet, indoor scene, etc.).
            NO_UNCLEAR   → Completely black, fully blurred, or impossible to identify any content.

            Be LENIENT with real photos: accept images taken from a car, at night, at an angle, or with partial views, as long as the subject is plausibly a road incident.
            Only reject if you are CERTAIN the image does not qualify.
            Reply with ONLY the code word. No punctuation, no explanation.
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

            // startsWith para tolerar texto extra que Gemini pueda añadir tras el código
            val accepted = when {
                raw.startsWith("YES")          -> { log.info("Gemini: ACEPTADA");                                              true  }
                raw.startsWith("NO_NOT_PHOTO") -> { log.warn("Gemini: RECHAZADA — imagen no fotográfica o animada");           false }
                raw.startsWith("NO_OBSCENE")   -> { log.warn("Gemini: RECHAZADA — contenido obsceno o inapropiado");           false }
                raw.startsWith("NO_UNRELATED") -> { log.warn("Gemini: RECHAZADA — no corresponde a un incidente vial");        false }
                raw.startsWith("NO_UNCLEAR")   -> { log.warn("Gemini: RECHAZADA — imagen irreconocible");                      false }
                raw.startsWith("NO")           -> { log.warn("Gemini: RECHAZADA — respuesta genérica NO: '$raw'");             false }
                raw.isEmpty()                  -> { log.warn("Gemini: respuesta vacía — rechazando por precaución");           false }
                else                           -> { log.warn("Gemini: respuesta inesperada '$raw' — aceptando con cautela");   true  }
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
