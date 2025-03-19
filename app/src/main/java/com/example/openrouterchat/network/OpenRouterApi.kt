interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun generateCompletion(
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

data class ChatCompletionRequest(
    val model: String = "gpt-3.5-turbo", // Can be configurable
    val messages: List<Message>,
    val temperature: Double = 0.7,
    val maxTokens: Int = 1000,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>
)

data class Choice(
    val message: Message,
    val finishReason: String
)
