package com.vialreport.backend.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.Base64

/** Resultado de la validación de IA */
data class ValidationResult(val approved: Boolean, val reason: String)

class PhotoAiService(private val apiKey: String) {

    private val log = LoggerFactory.getLogger(PhotoAiService::class.java)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // Modelo confirmado como disponible con Google AI Studio keys gratuitas
    private val MODEL = "gemini-1.5-flash"
    private val ENDPOINT = "https://generativelanguage.googleapis.com/v1/models/$MODEL:generateContent"

    suspend fun validate(imageBytes: ByteArray, mimeType: String): ValidationResult {
        if (apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY no configurado — validación omitida")
            return ValidationResult(true, "Sin validación (API key no configurada)")
        }

        val b64 = Base64.getEncoder().encodeToString(imageBytes)

        val prompt = """
            You are a content validator for a road incident reporting app.
            Analyze the image and reply with ONLY the word YES or NO.

            Reply YES if:
            - The image is a real photograph (taken with a camera, not drawn or animated)
            - AND it shows any outdoor public area related to roads or infrastructure:
              streets, roads, sidewalks, potholes, flooding, garbage on street,
              broken signs, broken traffic lights, landslides, cracks in pavement, streetlights.

            Reply NO if:
            - The image is a cartoon, anime, drawing, meme, illustration, or any non-photographic content
            - OR it contains nudity, sexual content, or graphic violence
            - OR it is a real photo showing only food, selfies, animals, or indoor scenes

            Your entire response must be a single word: YES or NO
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
            val httpResponse: HttpResponse = client.post("$ENDPOINT?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            // Captura el cuerpo como texto primero para poder loggearlo si hay error
            val bodyText = httpResponse.bodyAsText()
            log.info("Gemini HTTP ${httpResponse.status.value} — body preview: ${bodyText.take(300)}")

            if (!httpResponse.status.isSuccess()) {
                return ValidationResult(false, "Gemini HTTP ${httpResponse.status.value}: $bodyText")
            }

            val response = Json { ignoreUnknownKeys = true }.decodeFromString<GeminiResponse>(bodyText)

            val candidate    = response.candidates.firstOrNull()
            val finishReason = candidate?.finishReason ?: "NO_CANDIDATES"
            val raw          = candidate?.content?.parts?.firstOrNull()?.text?.trim()?.uppercase() ?: ""

            log.info("Gemini model=$MODEL raw='$raw' finishReason='$finishReason'")

            when {
                raw.startsWith("YES") ->
                    ValidationResult(true,  "Aceptada")

                raw.startsWith("NO") ->
                    ValidationResult(false, "Foto rechazada: no es una fotografía real de un incidente vial")

                finishReason == "SAFETY" ->
                    ValidationResult(false, "Foto rechazada: contenido inapropiado detectado")

                finishReason == "NO_CANDIDATES" ->
                    // El modelo no devolvió ningún candidato — puede ser error de API key o modelo no disponible
                    ValidationResult(false, "Error de validación: el modelo no respondió (verifica GEMINI_API_KEY en Render)")

                else ->
                    ValidationResult(false, "Error de validación: respuesta inesperada '$raw' (finishReason=$finishReason)")
            }

        } catch (e: Exception) {
            log.error("Gemini API error: ${e::class.simpleName}: ${e.message}")
            ValidationResult(false, "Error al contactar Gemini: ${e.message}")
        }
    }
}

// ── DTOs internos de la Gemini REST API ────────────────────────────

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
private data class InlineData(val mimeType: String, val data: String)

@Serializable
private data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList())

@Serializable
private data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)
