package com.inventtrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showAlertDetailDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxWidth()
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
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Buenos días,",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Hola, Andy \uD83D\uDC4B",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(onClick = { showNotificationDialog = true }) {
                    BadgedBox(
                        badge = {
                            Badge { Text("1") }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alerta roja de stock crítico
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAlertDetailDialog = true },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerta",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alerta de Stock Crítico",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Jarabe pediátrico alcanzó el stock mínimo",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Ver",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjetas de Resumen
            Text(
                text = "Resumen del inventario",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Total productos",
                    value = "148",
                    icon = Icons.Default.Inventory2
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Stock bajo",
                    value = "3",
                    isAlert = true,
                    icon = Icons.Default.ReportProblem
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Entradas hoy",
                    value = "+12",
                    valueColor = Color(0xFF2E7D32),
                    icon = Icons.Default.ArrowDownward
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Salidas hoy",
                    value = "-8",
                    valueColor = Color(0xFFC62828),
                    icon = Icons.Default.ArrowUpward
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Accesos rápidos
            Text(
                text = "Accesos rápidos",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickAccessButton(
                    icon = Icons.Default.SwapVert,
                    label = "Movimiento",
                    onClick = onRegisterMovementClick
                )
                QuickAccessButton(
                    icon = Icons.Default.AddCircleOutline,
                    label = "Producto",
                    onClick = onAddProductClick
                )
                QuickAccessButton(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    label = "Chatbot",
                    onClick = onOpenChatbotClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Últimos movimientos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos movimientos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Hoy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            MovementItem(
                name = "Azitromicina 500mg",
                time = "09:32",
                quantity = "+20",
                isPositive = true
            )
            MovementItem(
                name = "Suero fisiológico",
                time = "10:15",
                quantity = "-5",
                isPositive = false
            )
            MovementItem(
                name = "Jarabe pediátrico",
                time = "11:00",
                quantity = "+10",
                isPositive = true
            )
            MovementItem(
                name = "Ibuprofeno 400mg",
                time = "12:30",
                quantity = "-4",
                isPositive = false
            )
        }
    }

    // Modal de Notificaciones
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Notificaciones") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Jarabe pediátrico alcanzó el stock mínimo (2 unidades restantes).")
                    Text("• Registro de salida: Suero fisiológico (-5) realizado por Ana R.")
                    Text("• Sincronización de inventario completada correctamente.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Modal de detalle de alerta
    if (showAlertDetailDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDetailDialog = false },
            title = { Text("Detalle de Alerta de Stock") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Producto: Jarabe pediátrico 120ml", fontWeight = FontWeight.Bold)
                    Text("Existencia actual: 2 unidades")
                    Text("Stock mínimo configurado: 10 unidades")
                    Text("Recomendación: Registrar una entrada de reposición o contactar al proveedor.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    showAlertDetailDialog = false
                    onRegisterMovementClick()
                }) {
                    Text("Registrar entrada")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlertDetailDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    isAlert: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier.height(108.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
fun QuickAccessButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MovementItem(name: String, time: String, quantity: String, isPositive: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isPositive) Color(0xFF2E7D32) else Color(0xFFE65100))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = quantity,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }
    }
}