package com.example.openrouterchat.data

import com.example.openrouterchat.Model

data class ModelState(
    val model: Model,
    val currentConversation: Conversation? = null,
    val conversations: List<Conversation> = emptyList(),
    val settings: ModelSettings = ModelSettings()
)

data class ModelSettings(
    val temperature: Float = 0.7f,
    val topP: Float = 1f,
    val maxTokens: Int? = null
)
