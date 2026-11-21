package com.inventtrack.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventtrack.di.AppViewModelProvider
import com.inventtrack.ui.theme.AvailableGreen
import com.inventtrack.ui.theme.CriticalRed
import com.inventtrack.ui.theme.LowOrange
import com.inventtrack.ui.theme.SecondaryTeal
import com.inventtrack.ui.theme.Shapes
import java.time.format.DateTimeFormatter

@Composable
fun DashboardRoute(
    onRegisterMovementClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onOpenChatbotClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        onRegisterMovementClick = onRegisterMovementClick,
        onAddProductClick = onAddProductClick,
        onOpenChatbotClick = onOpenChatbotClick
    )
}

@Composable
fun DashboardScreen(
    userName: String,
    onRegisterMovementClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onOpenChatbotClick: () -> Unit
) {
    DashboardScreen(
        uiState = DashboardUiState(nombreUsuario = userName),
        onRegisterMovementClick = onRegisterMovementClick,
        onAddProductClick = onAddProductClick,
        onOpenChatbotClick = onOpenChatbotClick
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
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
                .widthIn(max = 800.dp)
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
                        color = SecondaryTeal.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil",
                                modifier = Modifier.size(28.dp),
                                tint = SecondaryTeal
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
                            text = if (uiState.nombreUsuario.isNotBlank()) "Hola, ${uiState.nombreUsuario} \uD83D\uDC4B" else "Hola \uD83D\uDC4B",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                IconButton(onClick = { showNotificationDialog = true }) {
                    if (uiState.alertas.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = CriticalRed) {
                                    Text("${uiState.alertas.size}", color = Color.White)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alerta roja de stock crítico si existen alertas
            if (uiState.alertas.isNotEmpty()) {
                val primerProductoCritico = uiState.productosCriticos.firstOrNull()
                val mensajeAlerta = primerProductoCritico?.let { "${it.nombre} alcanzó el stock mínimo" }
                    ?: uiState.alertas.first()
                Card(
                    colors = CardDefaults.cardColors(containerColor = CriticalRed.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAlertDetailDialog = true },
                    shape = Shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alerta",
                            tint = CriticalRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alerta de Stock Crítico",
                                style = MaterialTheme.typography.labelMedium,
                                color = CriticalRed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mensajeAlerta,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "Ver",
                            style = MaterialTheme.typography.labelSmall,
                            color = CriticalRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tarjetas de Resumen
            Text(
                text = "Resumen del inventario",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Total productos",
                    value = "${uiState.totalProductos}",
                    icon = Icons.Default.Inventory2,
                    valueColor = SecondaryTeal
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Stock bajo",
                    value = "${uiState.stockBajo}",
                    isAlert = uiState.stockBajo > 0,
                    icon = Icons.Default.ReportProblem,
                    valueColor = LowOrange
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
                    value = if (uiState.entradasHoy > 0) "+${uiState.entradasHoy}" else "${uiState.entradasHoy}",
                    valueColor = AvailableGreen,
                    icon = Icons.Default.ArrowDownward
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Salidas hoy",
                    value = if (uiState.salidasHoy > 0) "-${uiState.salidasHoy}" else "${uiState.salidasHoy}",
                    valueColor = CriticalRed,
                    icon = Icons.Default.ArrowUpward
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Accesos rápidos
            Text(
                text = "Accesos rápidos",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
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
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Recientes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.movimientosRecientes.isEmpty()) {
                Text(
                    text = "No hay movimientos registrados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
                uiState.movimientosRecientes.forEach { mov ->
                    MovementItem(
                        name = mov.productoNombre,
                        time = mov.fecha.format(timeFormatter),
                        quantity = if (mov.esEntrada) "+${mov.cantidad}" else "-${mov.cantidad}",
                        isPositive = mov.esEntrada
                    )
                }
            }
        }
    }

    // Modal de Notificaciones
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Notificaciones") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.alertas.isEmpty()) {
                        Text("• No hay alertas de stock bajo en este momento.")
                    } else {
                        uiState.alertas.forEach { alerta ->
                            Text("• $alerta")
                        }
                    }
                    Text("• Sincronización de inventario completada correctamente.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Cerrar", color = SecondaryTeal)
                }
            }
        )
    }

    // Modal de detalle de alerta
    if (showAlertDetailDialog) {
        val prod = uiState.productosCriticos.firstOrNull()
        AlertDialog(
            onDismissRequest = { showAlertDetailDialog = false },
            title = { Text("Detalle de Alerta de Stock") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (prod != null) {
                        Text("Producto: ${prod.nombre}", fontWeight = FontWeight.Bold)
                        Text("Existencia actual: ${prod.stockActual} unidades")
                        Text("Stock mínimo configurado: ${prod.stockMinimo} unidades")
                        Text("Recomendación: Registrar una entrada de reposición o contactar al proveedor.")
                    } else {
                        Text("No hay detalles disponibles para esta alerta.")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAlertDetailDialog = false
                        onRegisterMovementClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                ) {
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
    ElevatedCard(
        modifier = modifier.height(108.dp),
        shape = Shapes.medium
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
                    tint = if (isAlert) LowOrange else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
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
            shape = Shapes.medium,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = SecondaryTeal.copy(alpha = 0.15f),
                contentColor = SecondaryTeal
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = SecondaryTeal
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun MovementItem(name: String, time: String, quantity: String, isPositive: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = Shapes.medium
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
                        .background(if (isPositive) AvailableGreen else CriticalRed)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
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
                    color = if (isPositive) AvailableGreen else CriticalRed
                )
            }
        }
    }
}
