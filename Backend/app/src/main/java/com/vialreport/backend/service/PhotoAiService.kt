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
            You are a strict content moderator for a road incident reporting app used in Costa Rica.

            VALID INCIDENT TYPES (the only subjects allowed):
            - Inundación (flooded road or street)
            - Alumbrado público (broken or missing streetlight)
            - Basura acumulada (illegal dumping or garbage pile on public road)
            - Bache (pothole on road or sidewalk)
            - Señal dañada (damaged or missing road sign)
            - Semáforo dañado (broken or non-functional traffic light)
            - Derrumbe (landslide blocking a road)
            - Grieta en acera (crack in sidewalk or pavement)

            EVALUATION RULES — respond with EXACTLY one of these codes:
            • YES           → The image is a real photograph showing one of the valid incident types above.
            • NO_NOT_PHOTO  → The image is a cartoon, illustration, drawing, anime, meme, screenshot, AI-generated art, CGI, or any non-photographic content.
            • NO_OBSCENE    → The image contains nudity, sexual content, graphic violence, gore, or any offensive material.
            • NO_UNRELATED  → The image is a real photo but does NOT show any of the valid incident types (e.g. people, food, animals, indoor scenes, vehicles without damage, etc.).
            • NO_UNCLEAR    → The image is too blurry, dark, or cropped to identify its content.

            STRICT RULES:
            - A photo must show a real-world outdoor scene to be valid.
            - Even if only partially visible, obscene content → NO_OBSCENE.
            - Animated or illustrated images are NEVER valid, even if they depict road damage.
            - When in doubt, respond NO_UNRELATED.

            Respond with ONLY the code. No explanation.
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

            val code = response.candidates
                .firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?.trim()?.uppercase() ?: "NO_UNCLEAR"

            when {
                code == "YES" -> {
                    log.info("Gemini: imagen ACEPTADA")
                    true
                }
                code == "NO_NOT_PHOTO" -> {
                    log.warn("Gemini: RECHAZADA — contenido animado, ilustración o no fotográfico")
                    false
                }
                code == "NO_OBSCENE" -> {
                    log.warn("Gemini: RECHAZADA — contenido obsceno o inapropiado")
                    false
                }
                code == "NO_UNRELATED" -> {
                    log.warn("Gemini: RECHAZADA — imagen real pero no corresponde a un incidente vial")
                    false
                }
                code == "NO_UNCLEAR" -> {
                    log.warn("Gemini: RECHAZADA — imagen demasiado borrosa o sin contexto identificable")
                    false
                }
                else -> {
                    log.warn("Gemini: respuesta inesperada '$code' — rechazando por precaución")
                    false
                }
            }
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
