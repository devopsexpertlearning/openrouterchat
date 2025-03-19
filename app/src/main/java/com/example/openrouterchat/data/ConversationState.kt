package com.example.openrouterchat.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val modelId: String,
    val messages: List<MessageRequest> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ModelState(
    val model: Model,
    val conversations: List<Conversation> = emptyList(),
    val currentConversation: Conversation? = null,
    val settings: ModelSettings = ModelSettings()
)

data class ModelSettings(
    val temperature: Float = 0.7f,
    val topP: Float = 1f,
    val maxTokens: Int? = null
)
