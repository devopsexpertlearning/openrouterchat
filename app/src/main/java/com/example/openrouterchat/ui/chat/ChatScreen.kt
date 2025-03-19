@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var messageText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
            
            if (isLoading) {
                item {
                    LoadingIndicator()
                }
            }
        }

        // Input field
        ChatInput(
            value = messageText,
            onValueChange = { messageText = it },
            onSendClick = {
                if (messageText.isNotBlank()) {
                    viewModel.sendMessage(messageText)
                    messageText = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val backgroundColor = when {
        message.isError -> MaterialTheme.colorScheme.errorContainer
        isUser -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(if (isUser) Alignment.End else Alignment.Start)
                )
            }
        }
    }
}

@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Type a message") },
                modifier = Modifier.weight(1f),
                maxLines = 3
            )
            
            IconButton(onClick = onSendClick) {
                Icon(Icons.Default.Send, "Send message")
            }
        }
    }
}
