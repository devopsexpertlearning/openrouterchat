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

// DataStore for API Key
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val API_KEY = stringPreferencesKey("api_key")

data class Message(val text: String, val isUser: Boolean)

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
}

@Composable
fun OpenRouterChatApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Load API key from DataStore
    var apiKey by remember { mutableStateOf<String?>(null) }
    rememberCoroutineScope().launch {
        apiKey = context.dataStore.data.map { preferences ->
            preferences[API_KEY]
        }.first()
    }

    // Database instance
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "chat-database"
        ).build()
    }

    NavHost(navController = navController, startDestination = if (apiKey.isNullOrEmpty()) "api_key" else "chat") {
        composable("api_key") {
            ApiKeyScreen(onApiKeyEntered = { key ->
                coroutineScope.launch {
                    context.dataStore.edit { settings ->
                        settings[API_KEY] = key
                    }
                    apiKey = key // Update the apiKey state
                    navController.navigate("chat")
                }
            })
        }
        composable("chat") {
            ChatScreen(apiKey = apiKey ?: "", db)
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
//    var chatHistory by remember { mutableStateOf(listOf<ChatHistory>()) } // No longer needed

    // Fetch models on initial load
    LaunchedEffect(apiKey) {
        if (apiKey.isNotBlank()) {
            try {
                isLoading = true
                models = api.getModels()
                if (models.isNotEmpty()) {
                    selectedModel = models[0].id // Select the first model by default
                }
            } catch (e: OpenRouterApiException) {
                errorMessage = e.message
                Toast.makeText(context, "Failed to load", Toast.LENGTH_LONG).show()

            } finally {
                isLoading = false
            }
        }
    }

   // Load chat history and populate messages
    LaunchedEffect(Unit, selectedModel) {
        chatHistoryDao.getAll().collect { history ->
            messages.clear()
            history.filter { it.model == selectedModel }.forEach { chatHistory ->
                    messages.addAll(chatHistory.messages)
                }
        }
    }

    // Delete old chats
    LaunchedEffect(Unit) {
        val tenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
        chatHistoryDao.deleteOldChats(tenDaysAgo)
    }


    // Dispose of the API client when the composable is disposed
    DisposableEffect(api) {
        onDispose { api.close() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Model Selection Dropdown
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Button(onClick = { expanded = true }, modifier = Modifier.padding(end = 8.dp)) {
                Text(selectedModel ?: "Select Model")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                models.forEach { model ->
                    DropdownMenuItem(onClick = {
                        selectedModel = model.id
                        expanded = false
                         // Clear messages when model changes
                    }, text = {Text(model.name)})
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                label = { Text("Message") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (newMessage.isNotBlank() && selectedModel != null) {
                    // Add user message immediately
                    messages.add(0, Message(newMessage, true))

                    coroutineScope.launch {
                        try {
                            val response = api.sendMessage(selectedModel!!, newMessage)
                            val botMessage = Message(response.choices[0].message.content, false)
                            messages.add(0, botMessage) // Add to the messages list

                            // Save to database, appending to existing history
                            val currentChatHistory = chatHistoryDao.getAll().first().firstOrNull { it.model == selectedModel }
                            val updatedMessages = if (currentChatHistory != null) {
                                currentChatHistory.messages + listOf(Message(newMessage, true), botMessage)
                            } else {
                                listOf(Message(newMessage, true), botMessage)
                            }
                            val newChatHistory = ChatHistory(model = selectedModel!!, messages = updatedMessages)

                            if (currentChatHistory != null) {
                                chatHistoryDao.delete(currentChatHistory) // Delete old entry
                            }
                            chatHistoryDao.insert(newChatHistory) // Insert new entry


                        } catch (e: OpenRouterApiException) {
                            errorMessage = e.message
                            Toast.makeText(context, "Failed to send message", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                            newMessage = ""
                        }
                    }
                }
            }) {
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val backgroundColor = if (message.isUser) Purple40 else PurpleGrey40
    val textColor = if (message.isUser) Color.White else Color.Black
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            modifier = Modifier
                .align(alignment)
                .padding(8.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
                color = textColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    OpenRouterChatTheme {
        OpenRouterChatApp()
    }
}