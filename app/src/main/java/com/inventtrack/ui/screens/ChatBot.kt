package com.inventtrack.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.inventtrack.ui.components.InventTrackTopBar
import com.inventtrack.ui.theme.ChatBubbleBot
import com.inventtrack.ui.theme.ChatBubbleUser
import com.inventtrack.ui.theme.SecondaryTeal

data class ChatMessage(val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvenBotScreen() {
    var textState by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("¡Hola! Soy InvenBot. ¿En qué puedo ayudarte con tu inventario hoy?", isUser = false),
                ChatMessage("¿Stock de Azitromicina 500mg?", isUser = true),
                ChatMessage("Actualmente tienes 5 cajas disponibles.", isUser = false)
            )
        )
    }

    val suggestions = listOf("¿Qué productos tienen stock bajo?", "¿Qué entradas hubo hoy?", "Stock de Paracetamol")
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
                            messages = messages + ChatMessage(suggestion, isUser = true)
                            messages = messages + ChatMessage("Esta es una respuesta simulada para: '$suggestion'.", isUser = false)
                        },
                        label = { Text(suggestion, style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium) },
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
                    value = textState,
                    onValueChange = { textState = it },
                    placeholder = { Text("Escribe una pregunta...", style = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryTeal
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textState.isNotBlank()) {
                            messages = messages + ChatMessage(textState, isUser = true)
                            textState = ""
                            messages = messages + ChatMessage("Respuesta simulada generada por InvenBot.", isUser = false)
                        }
                    },
                    modifier = Modifier
                        .size(if (isLandscape) 42.dp else 48.dp)
                        .background(SecondaryTeal, shape = RoundedCornerShape(24.dp))
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
