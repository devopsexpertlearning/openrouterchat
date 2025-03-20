package com.example.openrouterchat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openrouterchat.ChatRepository
import com.example.openrouterchat.Model
import com.example.openrouterchat.data.ModelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _modelStates = MutableStateFlow<Map<String, ModelState>>(emptyMap())
    val modelStates: StateFlow<Map<String, ModelState>> = _modelStates.asStateFlow()

    private val _currentModel = MutableStateFlow<Model?>(null)
    val currentModel: StateFlow<Model?> = _currentModel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getModels().collect { models ->
                _modelStates.value = models.associate { model ->
                    model.id to ModelState(model = model)
                }
            }
        }
    }

    fun selectModel(modelId: String) {
        val model = _modelStates.value[modelId]?.model ?: return
        _currentModel.value = model
        
        // Create new conversation if needed
        if (_modelStates.value[modelId]?.currentConversation == null) {
            createNewConversation(modelId)
        }
    }

    fun createNewConversation(modelId: String) {
        val modelState = _modelStates.value[modelId] ?: return
        val newConversation = Conversation(modelId = modelId)
        
        _modelStates.value = _modelStates.value.toMutableMap().apply {
            put(modelId, modelState.copy(
                currentConversation = newConversation,
                conversations = modelState.conversations + newConversation
            ))
        }
    }

    fun sendMessage(message: String) {
        val modelId = _currentModel.value?.id ?: return
        val modelState = _modelStates.value[modelId] ?: return
        val conversation = modelState.currentConversation ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val messages = conversation.messages + MessageRequest("user", message)
                
                val response = repository.sendMessage(
                    modelId = modelId,
                    messages = messages,
                    temperature = modelState.settings.temperature,
                    topP = modelState.settings.topP,
                    maxTokens = modelState.settings.maxTokens
                )

                // Update conversation with new messages
                val updatedMessages = messages + MessageRequest(
                    role = "assistant",
                    content = response.choices.first().message.content
                )
                
                updateConversation(modelId, conversation.copy(
                    messages = updatedMessages,
                    updatedAt = System.currentTimeMillis()
                ))
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateConversation(modelId: String, conversation: Conversation) {
        _modelStates.value = _modelStates.value.toMutableMap().apply {
            val modelState = get(modelId) ?: return
            put(modelId, modelState.copy(
                currentConversation = conversation,
                conversations = modelState.conversations.map {
                    if (it.id == conversation.id) conversation else it
                }
            ))
        }
    }
}
