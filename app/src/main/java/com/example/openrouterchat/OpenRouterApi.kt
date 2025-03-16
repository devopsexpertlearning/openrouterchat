package com.example.openrouterchat

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class OpenRouterApi(private val apiKey: String) {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    suspend fun getModels(): List<Model> {
        try {
            val response = client.get("https://openrouter.ai/api/v1/models") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }
            return response.body<ModelListResponse>().data
        } catch (e: Exception) {
            throw OpenRouterApiException("Failed to fetch models: ${e.message}", e)
        }
    }

    suspend fun sendMessage(model: String, message: String): ChatCompletionResponse {
        try {
            val requestBody = ChatCompletionRequest(
                model = model,
                messages = listOf(
                    MessageRequest(role = "user", content = message)
                )
            )
            val response = client.post("https://openrouter.ai/api/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            return response.body()
        } catch (e: Exception) {
            throw OpenRouterApiException("Failed to send message: ${e.message}", e)
        }
    }

    fun close() {
        client.close()
    }
}

@Serializable
data class ModelListResponse(
    val data: List<Model>
)

@Serializable
data class Model(
    val id: String,
    val name: String,
    val context_length: Int
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<MessageRequest>,
    val temperature: Float? = null,
    val top_p: Float? = null,
    val max_tokens: Int? = null,
    val stream: Boolean? = false
)

@Serializable
data class MessageRequest(
    val role: String,
    val content: String
)
@Serializable
data class ChatCompletionResponse(
    val id: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>
)
@Serializable
data class Choice(
    val index: Int,
    val message: OpenRouterMessage,
    val finish_reason: String?
)

@Serializable
data class OpenRouterMessage(
    val role: String,
    val content: String
)

class OpenRouterApiException(message: String, cause: Throwable? = null) : Exception(message, cause)