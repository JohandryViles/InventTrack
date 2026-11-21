package com.inventtrack.ui.chatbot

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventtrack.data.model.ChatMessage
import com.inventtrack.di.AppViewModelProvider
import com.inventtrack.ui.components.InventTrackTopBar
import com.inventtrack.ui.theme.ChatBubbleBot
import com.inventtrack.ui.theme.ChatBubbleUser
import com.inventtrack.ui.theme.SecondaryTeal

@Composable
fun ChatRoute(
    viewModel: ChatViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val input by viewModel.input.collectAsStateWithLifecycle()
    val isTyping by viewModel.isTyping.collectAsStateWithLifecycle()

    InvenBotScreen(
        messages = messages,
        input = input,
        isTyping = isTyping,
        onInputChange = viewModel::onInputChange,
        onSendMessage = viewModel::send
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvenBotScreen(
    messages: List<ChatMessage> = emptyList(),
    input: String = "",
    isTyping: Boolean = false,
    onInputChange: (String) -> Unit = {},
    onSendMessage: (String?) -> Unit = {}
) {
    val suggestions = listOf("¿Qué productos tienen stock bajo?", "¿Qué entradas hubo hoy?", "Stock de Paracetamol")
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isTyping) {
        val totalCount = messages.size + if (isTyping) 1 else 0
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    Scaffold(
        topBar = {
            if (!isLandscape) {
                InventTrackTopBar(title = "InvenBot")
            } else {
                CenterAlignedTopAppBar(
                    title = { Text("InvenBot", style = MaterialTheme.typography.titleMedium, color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.height(40.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
                    .padding(horizontal = if (isLandscape) 8.dp else 16.dp),
                contentPadding = PaddingValues(vertical = if (isLandscape) 6.dp else 12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (isTyping) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .background(
                                        color = ChatBubbleBot,
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = 4.dp,
                                            bottomEnd = 16.dp
                                        )
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "InvenBot está escribiendo...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .padding(
                        horizontal = if (isLandscape) 8.dp else 16.dp,
                        vertical = if (isLandscape) 2.dp else 6.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { suggestion ->
                    SuggestionChip(
                        onClick = {
                            if (!isTyping) {
                                onSendMessage(suggestion)
                            }
                        },
                        enabled = !isTyping,
                        label = {
                            Text(
                                suggestion,
                                style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .padding(
                        horizontal = if (isLandscape) 8.dp else 16.dp,
                        vertical = if (isLandscape) 4.dp else 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    placeholder = {
                        Text(
                            "Escribe una pregunta...",
                            style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isTyping,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (input.isNotBlank() && !isTyping) {
                            onSendMessage(null)
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryTeal
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (input.isNotBlank() && !isTyping) {
                            onSendMessage(null)
                        }
                    },
                    enabled = input.isNotBlank() && !isTyping,
                    modifier = Modifier
                        .size(if (isLandscape) 42.dp else 48.dp)
                        .background(
                            if (input.isNotBlank() && !isTyping) SecondaryTeal else SecondaryTeal.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (message.isUser) ChatBubbleUser else ChatBubbleBot,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser) Color.White else Color.Black
            )
        }
    }
}
