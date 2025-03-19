data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val role: String, // "user" or "assistant" 
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)
