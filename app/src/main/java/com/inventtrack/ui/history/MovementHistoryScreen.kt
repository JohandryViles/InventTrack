package com.inventtrack.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventtrack.data.model.Movimiento
import com.inventtrack.di.AppViewModelProvider
import com.inventtrack.ui.theme.AvailableGreen
import com.inventtrack.ui.theme.CriticalRed
import com.inventtrack.ui.theme.PrimaryDarkBlue
import com.inventtrack.ui.theme.SecondaryTeal
import com.inventtrack.ui.theme.Shapes
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MovementHistoryRoute(
    onBackClick: () -> Unit,
    viewModel: MovementHistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MovementHistoryScreen(
        uiState = uiState,
        onDateFilterChange = viewModel::onDateFilterChange,
        onTypeFilterChange = viewModel::onTypeFilterChange,
        onProductFilterChange = viewModel::onProductFilterChange,
        onClearFilters = viewModel::clearFilters,
        onSelectMovement = viewModel::selectMovement,
        onDismissDetail = viewModel::dismissDetail,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementHistoryScreen(
    uiState: MovementHistoryUiState,
    onDateFilterChange: (String) -> Unit,
    onTypeFilterChange: (String) -> Unit,
    onProductFilterChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onSelectMovement: (Movimiento) -> Unit,
    onDismissDetail: () -> Unit,
    onBackClick: () -> Unit
) {
    // Los estados puramente visuales de menú desplegable sí pueden quedarse en remember
    var dateMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var productMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Historial de movimientos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Registro completo de entradas y salidas",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (uiState.isAnyFilterActive) {
                        IconButton(onClick = onClearFilters) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar filtros",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryDarkBlue)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Sección de Filtros interactivos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = SecondaryTeal,
                        modifier = Modifier.size(20.dp)
                    )

                    // Filtro por Fecha
                    Box {
                        FilterChipButton(
                            label = if (uiState.selectedDate == "Todas") "Fecha" else uiState.selectedDate,
                            isActive = uiState.selectedDate != "Todas",
                            onClick = { dateMenuExpanded = true }
                        )
                        DropdownMenu(
                            expanded = dateMenuExpanded,
                            onDismissRequest = { dateMenuExpanded = false }
                        ) {
                            listOf("Todas", "Hoy", "Ayer").forEach { dateOption ->
                                DropdownMenuItem(
                                    text = { Text(dateOption) },
                                    onClick = {
                                        onDateFilterChange(dateOption)
                                        dateMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Filtro por Tipo
                    Box {
                        FilterChipButton(
                            label = when (uiState.selectedType) {
                                "ENTRADA" -> "Entradas"
                                "SALIDA" -> "Salidas"
                                else -> "Tipo"
                            },
                            isActive = uiState.selectedType != "Todos",
                            onClick = { typeMenuExpanded = true }
                        )
                        DropdownMenu(
                            expanded = typeMenuExpanded,
                            onDismissRequest = { typeMenuExpanded = false }
                        ) {
                            listOf(
                                "Todos" to "Todos",
                                "ENTRADA" to "Entradas",
                                "SALIDA" to "Salidas"
                            ).forEach { (typeVal, typeLabel) ->
                                DropdownMenuItem(
                                    text = { Text(typeLabel) },
                                    onClick = {
                                        onTypeFilterChange(typeVal)
                                        typeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Filtro por Producto (las opciones salen de los datos y no de un remember)
                    Box {
                        FilterChipButton(
                            label = if (uiState.selectedProduct == "Todos") {
                                "Producto"
                            } else {
                                uiState.selectedProduct.take(10) + if (uiState.selectedProduct.length > 10) "…" else ""
                            },
                            isActive = uiState.selectedProduct != "Todos",
                            onClick = { productMenuExpanded = true }
                        )
                        DropdownMenu(
                            expanded = productMenuExpanded,
                            onDismissRequest = { productMenuExpanded = false }
                        ) {
                            uiState.productOptions.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text(prod) },
                                    onClick = {
                                        onProductFilterChange(prod)
                                        productMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SecondaryTeal)
                    }
                } else if (uiState.groupedMovements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No se encontraron movimientos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Intenta modificando o limpiando los filtros seleccionados.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onClearFilters,
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                            ) {
                                Text("Restablecer filtros", color = Color.White)
                            }
                        }
                    }
                } else {
                    // Lista agrupada por día ya filtrada
                    uiState.groupedMovements.forEach { (dateHeader, movementsInGroup) ->
                        Text(
                            text = dateHeader,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        movementsInGroup.forEach { movement ->
                            HistoryItemCard(
                                movement = movement,
                                onClick = { onSelectMovement(movement) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal con detalles del movimiento al pulsar un elemento (estado vive en el ViewModel)
    uiState.selectedMovement?.let { mov ->
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale.forLanguageTag("es"))
        AlertDialog(
            onDismissRequest = onDismissDetail,
            title = { Text("Detalle del Movimiento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Producto: ${mov.productoNombre}", fontWeight = FontWeight.Bold)
                    Text("Tipo: ${mov.tipo.name} (${if (mov.esEntrada) "Ingreso a almacén" else "Salida de almacén"})")
                    Text("Cantidad: ${mov.cantidad} unidades")
                    Text("Fecha: ${mov.fecha.format(dateFormatter)} a las ${mov.fecha.format(timeFormatter)}")
                    Text("Registrado por: ${mov.usuario}")
                    if (mov.nota.isNotBlank()) {
                        Text("Observación: ${mov.nota}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissDetail) {
                    Text("Cerrar", color = SecondaryTeal)
                }
            }
        )
    }
}

@Composable
fun FilterChipButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isActive) SecondaryTeal else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = Shapes.small,
        modifier = modifier
            .height(36.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun HistoryItemCard(
    movement: Movimiento,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Etiqueta visual de Entrada/Salida con estilo consistente
                Surface(
                    color = if (movement.esEntrada) AvailableGreen.copy(alpha = 0.18f) else CriticalRed.copy(alpha = 0.18f),
                    shape = Shapes.small,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = movement.tipo.name,
                        color = if (movement.esEntrada) AvailableGreen else CriticalRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Column {
                    Text(
                        text = movement.productoNombre,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${movement.usuario} - ${movement.fecha.format(timeFormatter)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = if (movement.esEntrada) "+${movement.cantidad}" else "-${movement.cantidad}",
                fontWeight = FontWeight.Bold,
                color = if (movement.esEntrada) AvailableGreen else CriticalRed,
                fontSize = 17.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
