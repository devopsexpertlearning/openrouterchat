package com.example.openrouterchat

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
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
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

class OpenRouterApi @Inject constructor(
    private val apiKeyProvider: ApiKeyProvider
) {

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

    private suspend fun getAuthHeader(): String {
        val apiKey = apiKeyProvider.getApiKey() 
            ?: throw OpenRouterApiException("API key not found")
        return "Bearer $apiKey"
    }

    suspend fun getModels(): List<Model> {
        try {
            val response: ModelListResponse = client.get("https://openrouter.ai/api/v1/models") {
                header("Authorization", getAuthHeader())
            }.body()
            return response.data
        } catch (e: Exception) {
            throw OpenRouterApiException("Failed to fetch models: ${e.message}", e)
        }
    }

    suspend fun sendMessage(
        model: String, 
        messages: List<MessageRequest>,
        temperature: Float? = null,
        topP: Float? = null,
        maxTokens: Int? = null
    ): ChatCompletionResponse {
        try {
            return client.post("https://openrouter.ai/api/v1/chat/completions") {
                header("Authorization", getAuthHeader())
                contentType(ContentType.Application.Json)
                setBody(ChatCompletionRequest(
                    model = model,
                    messages = messages,
                    temperature = temperature,
                    top_p = topP,
                    max_tokens = maxTokens
                ))
            }.body()
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
    val context_length: Int,
    val description: String? = null,
    val top_p: Float = 1f,
    val temperature: Float = 0.7f,
    val pricing: ModelPricing? = null
)

@Serializable
data class ModelPricing(
    @SerialName("prompt_cost") val promptCost: Double,
    @SerialName("completion_cost") val completionCost: Double
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<MessageRequest>,
    val temperature: Float? = null,
    val top_p: Float? = null,
    val max_tokens: Int? = null,
    val stream: Boolean? = false,
    @SerialName("stop_on_function_call") val stopOnFunctionCall: Boolean? = false,
    val functions: List<FunctionDefinition>? = null
)

@Serializable
data class FunctionDefinition(
    val name: String,
    val description: String,
    val parameters: JsonObject
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