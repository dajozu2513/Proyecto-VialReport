package com.vialreport.backend.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
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

    // Returns true if the image appears to be a real road incident
    suspend fun isRoadIncident(imageBytes: ByteArray, mimeType: String): Boolean {
        if (apiKey.isBlank()) {
            log.warn("ANTHROPIC_API_KEY not set — skipping AI photo filter")
            return true
        }

        val b64 = Base64.getEncoder().encodeToString(imageBytes)

        val requestBody = AnthropicRequest(
            model = "claude-haiku-4-5-20251001",
            maxTokens = 64,
            messages = listOf(
                AnthropicMessage(
                    role = "user",
                    content = listOf(
                        ContentBlock.Image(
                            source = ImageSource(
                                type = "base64",
                                mediaType = mimeType,
                                data = b64
                            )
                        ),
                        ContentBlock.Text(
                            text = "Does this image show a real road or public infrastructure incident (pothole, damaged sign, flooded road, broken traffic light, cracked sidewalk, fallen debris, etc.)? Reply with exactly one word: YES or NO."
                        )
                    )
                )
            )
        )

        return try {
            val response: AnthropicResponse = client.post("https://api.anthropic.com/v1/messages") {
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            val answer = response.content.firstOrNull()?.text?.trim()?.uppercase() ?: "NO"
            log.info("AI photo verdict: $answer")
            answer.startsWith("YES")
        } catch (e: Exception) {
            log.error("AI photo check failed: ${e.message} — allowing upload")
            true
        }
    }
}

// ── Anthropic API DTOs (internal) ────────────────────────────────

@Serializable
private data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val messages: List<AnthropicMessage>
)

@Serializable
private data class AnthropicMessage(
    val role: String,
    val content: List<ContentBlock>
)

@Serializable
private sealed class ContentBlock {
    @Serializable
    @SerialName("image")
    data class Image(val source: ImageSource) : ContentBlock()

    @Serializable
    @SerialName("text")
    data class Text(val text: String) : ContentBlock()
}

@Serializable
private data class ImageSource(
    val type: String,
    @SerialName("media_type") val mediaType: String,
    val data: String
)

@Serializable
private data class AnthropicResponse(
    val content: List<ResponseContent> = emptyList()
)

@Serializable
private data class ResponseContent(
    val type: String = "",
    val text: String = ""
)
