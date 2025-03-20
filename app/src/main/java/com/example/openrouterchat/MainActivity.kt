package com.example.openrouterchat

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.room.Room
import com.example.openrouterchat.ui.theme.OpenRouterChatTheme
import com.example.openrouterchat.ui.theme.Purple40
import com.example.openrouterchat.ui.theme.PurpleGrey40
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import dagger.hilt.android.AndroidEntryPoint

// DataStore for API Key
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val API_KEY = stringPreferencesKey("api_key")

data class Message(
    val text: String, 
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val id: String = UUID.randomUUID().toString(),
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENT, ERROR, LOADING
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenRouterChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OpenRouterChatApp()
                }
            }
        }
    }

    @Composable
    fun OpenRouterChatApp() {
        val navController = rememberNavController()
        val apiKeyProvider = remember { ApiKeyProvider(LocalContext.current) }
        var hasApiKey by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var apiKey by remember { mutableStateOf<String?>(null) }
        
        LaunchedEffect(Unit) {
            apiKeyProvider.apiKey.collect { key ->
                apiKey = key
                hasApiKey = !key.isNullOrEmpty()
            }
        }

        NavHost(
            navController = navController,
            startDestination = if (hasApiKey) "chat" else "api_key"
        ) {
            composable("api_key") {
                ApiKeyScreen(
                    onApiKeyEntered = { key ->
                        scope.launch {
                            apiKeyProvider.setApiKey(key)
                            navController.navigate("chat") {
                                popUpTo("api_key") { inclusive = true }
                            }
                        }
                    }
                )
            }
            composable("chat") {
                ChatScreen(apiKey = apiKey ?: "", db)
            }
        }
    }
}

@Composable
fun ApiKeyScreen(onApiKeyEntered: (String) -> Unit) {
    var apiKeyInput by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter your OpenRouter API Key:")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            label = { Text("API Key") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onApiKeyEntered(apiKeyInput) },
            enabled = apiKeyInput.isNotBlank()
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun ChatScreen(apiKey: String, db: AppDatabase) {
    val messages = remember { mutableStateListOf<Message>() }
    var newMessage by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf<String?>(null) }
    var models by remember { mutableStateOf(listOf<Model>()) }
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val api = remember { OpenRouterApi(apiKey) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val chatHistoryDao = db.chatHistoryDao()
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh = {
        coroutineScope.launch {
            refreshing = true
            // Reload chat history
            chatHistoryDao.getAll().collect { history ->
                messages.clear()
                history.filter { it.model == selectedModel }.forEach { chatHistory ->
                    messages.addAll(chatHistory.messages)
                }
            }
            refreshing = false
        }
    })

    LaunchedEffect(apiKey) {
        if (apiKey.isNotBlank()) {
            try {
                isLoading = true
                models = api.getModels()
                if (models.isNotEmpty()) {
                    selectedModel = models[0].id
                }
            } catch (e: OpenRouterApiException) {
                errorMessage = e.message
                Toast.makeText(context, "Failed to load models", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit, selectedModel) {
        chatHistoryDao.getAll().collect { history ->
            messages.clear()
            history.filter { it.model == selectedModel }.forEach { chatHistory ->
                messages.addAll(chatHistory.messages)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Model Selection
            TopAppBar(
                title = { Text("OpenRouter Chat") },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Text(selectedModel ?: "Select Model")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            models.forEach { model ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedModel = model.id
                                        expanded = false
                                        messages.clear()
                                    },
                                    text = { Text(model.name) }
                                )
                            }
                        }
                    }
                    // Clear Chat History Button
                    IconButton(onClick = {
                        coroutineScope.launch {
                            selectedModel?.let { model ->
                                chatHistoryDao.deleteByModel(model)
                                messages.clear()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Delete, "Clear History")
                    }
                }
            )

            // Chat Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true,
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        onRetry = { failedMessage ->
                            if (selectedModel != null && failedMessage.status == MessageStatus.ERROR) {
                                coroutineScope.launch {
                                    val retryIndex = messages.indexOf(failedMessage)
                                    if (retryIndex >= 0) {
                                        messages[retryIndex] = failedMessage.copy(status = MessageStatus.LOADING)
                                        try {
                                            val response = api.sendMessage(selectedModel!!, failedMessage.text)
                                            val botMessage = Message(
                                                response.choices[0].message.content,
                                                false
                                            )
                                            messages[retryIndex] = botMessage
                                            
                                            // Update database
                                            val currentHistory = chatHistoryDao.getByModel(selectedModel!!).firstOrNull()
                                            val updatedMessages = currentHistory?.messages?.toMutableList() ?: mutableListOf()
                                            updatedMessages.add(botMessage)
                                            val newHistory = ChatHistory(
                                                model = selectedModel!!,
                                                messages = updatedMessages
                                            )
                                            if (currentHistory != null) {
                                                chatHistoryDao.delete(currentHistory)
                                            }
                                            chatHistoryDao.insert(newHistory)
                                            
                                        } catch (e: Exception) {
                                            messages[retryIndex] = failedMessage.copy(status = MessageStatus.ERROR)
                                            errorMessage = e.message
                                            Toast.makeText(context, "Failed to retry message", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Input Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newMessage.isNotBlank() && selectedModel != null) {
                                val userMessage = Message(newMessage, true)
                                messages.add(0, userMessage)
                                val botMessage = Message("", false, status = MessageStatus.LOADING)
                                messages.add(0, botMessage)
                                
                                coroutineScope.launch {
                                    try {
                                        val response = api.sendMessage(selectedModel!!, newMessage)
                                        val updatedBotMessage = botMessage.copy(
                                            text = response.choices[0].message.content,
                                            status = MessageStatus.SENT
                                        )
                                        val messageIndex = messages.indexOf(botMessage)
                                        if (messageIndex >= 0) {
                                            messages[messageIndex] = updatedBotMessage
                                        }

                                        // Update database
                                        val currentHistory = chatHistoryDao.getByModel(selectedModel!!).firstOrNull()
                                        val updatedMessages = currentHistory?.messages?.toMutableList() ?: mutableListOf()
                                        updatedMessages.addAll(listOf(userMessage, updatedBotMessage))
                                        val newHistory = ChatHistory(
                                            model = selectedModel!!,
                                            messages = updatedMessages
                                        )
                                        if (currentHistory != null) {
                                            chatHistoryDao.delete(currentHistory)
                                        }
                                        chatHistoryDao.insert(newHistory)

                                    } catch (e: Exception) {
                                        val messageIndex = messages.indexOf(botMessage)
                                        if (messageIndex >= 0) {
                                            messages[messageIndex] = botMessage.copy(status = MessageStatus.ERROR)
                                        }
                                        errorMessage = e.message
                                        Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                newMessage = ""
                            }
                        },
                        enabled = newMessage.isNotBlank() && selectedModel != null
                    ) {
                        Icon(Icons.Default.Send, "Send")
                    }
                }
            }
        }

        PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
fun MessageBubble(message: Message, onRetry: (Message) -> Unit) {
    val backgroundColor = if (message.isUser) Purple40 else PurpleGrey40
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (message.status == MessageStatus.LOADING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = message.text,
                        color = if (message.isUser) Color.White else Color.Black
                    )
                }
                
                if (message.status == MessageStatus.ERROR) {
                    IconButton(onClick = { onRetry(message) }) {
                        Icon(Icons.Default.Refresh, "Retry", tint = Color.Red)
                    }
                }
            }
        }
        
        Text(
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp),
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    OpenRouterChatTheme {
        OpenRouterChatApp()
    }
}