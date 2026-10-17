package com.inventtrack.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    onRegisterMovementClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onOpenChatbotClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Cabecera: Saludo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Buenos días,", fontSize = 14.sp, color = Color.Gray)
                    Text(text = "Hola, Andy \uD83D\uDC4B", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(onClick = { /* Acción de notificaciones */ }) {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notificaciones")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alerta roja de stock crítico
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = "Alerta", tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jarabe pediátrico alcanzó el stock mínimo",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjetas de Resumen
        Text(text = "Resumen del inventario", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCard(modifier = Modifier.weight(1f), title = "Total productos", value = "148")
            SummaryCard(modifier = Modifier.weight(1f), title = "Stock bajo", value = "3", isAlert = true)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCard(modifier = Modifier.weight(1f), title = "Entradas hoy", value = "+12", valueColor = Color(0xFF4CAF50))
            SummaryCard(modifier = Modifier.weight(1f), title = "Salidas hoy", value = "-8", valueColor = Color.Red)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Accesos rápidos
        Text(text = "Accesos rápidos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickAccessButton(icon = Icons.Default.Add, label = "Movimiento", onClick = onRegisterMovementClick)
            QuickAccessButton(icon = Icons.Default.AddCircle, label = "Producto", onClick = onAddProductClick)
            QuickAccessButton(icon = Icons.Default.Build, label = "Chatbot", onClick = onOpenChatbotClick) // Usando un ícono temporal para chatbot
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Últimos movimientos
        Text(text = "Últimos movimientos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        MovementItem(name = "Azitromicina 500mg", time = "09:32", quantity = "+20", isPositive = true)
        MovementItem(name = "Suero fisiológico", time = "10:15", quantity = "-5", isPositive = false)
        MovementItem(name = "Jarabe pediátrico", time = "11:00", quantity = "+10", isPositive = true)
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    isAlert: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAlert) MaterialTheme.colorScheme.error else valueColor
            )
        }
    }
}

@Composable
fun QuickAccessButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(64.dp)) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp)
    }
}

@Composable
fun MovementItem(name: String, time: String, quantity: String, isPositive: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF9800), shape = RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = name, fontWeight = FontWeight.Medium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = time, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = quantity,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) Color(0xFF4CAF50) else Color.Red
                )
            }
        }
    }
}