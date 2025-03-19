class ChatRepository @Inject constructor(
    private val api: OpenRouterApi,
    private val messageDao: ChatMessageDao
) {
    suspend fun sendMessage(message: String): Flow<ChatMessage> = flow {
        try {
            // Save user message
            val userMessage = ChatMessage(content = message, role = "user")
            messageDao.insert(userMessage)
            emit(userMessage)

            // Make API request
            val response = api.generateCompletion(
                ChatCompletionRequest(
                    messages = listOf(
                        Message(role = "user", content = message)
                    )
                )
            )

            // Save assistant response
            val assistantMessage = ChatMessage(
                content = response.choices.first().message.content,
                role = "assistant"
            )
            messageDao.insert(assistantMessage)
            emit(assistantMessage)

        } catch (e: Exception) {
            emit(ChatMessage(
                content = "Error: ${e.message}",
                role = "assistant",
                isError = true
            ))
        }
    }

    fun getMessages(): Flow<List<ChatMessage>> {
        return messageDao.getAllMessages()
    }
}
