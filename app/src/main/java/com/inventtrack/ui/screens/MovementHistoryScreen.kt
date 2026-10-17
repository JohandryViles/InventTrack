package com.inventtrack.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class HistoryMovement(
    val id: Int,
    val dateGroup: String, // "Hoy — 21 Sep 2026", "Ayer — 20 Sep 2026"
    val dayKey: String,    // "Hoy", "Ayer"
    val type: String,      // "ENTRADA", "SALIDA"
    val product: String,
    val userTime: String,
    val quantity: String,
    val isEntry: Boolean,
    val note: String = "Operación estándar registrada en sistema."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementHistoryScreen(
    onBackClick: () -> Unit
) {
    // Datos mock del historial
    val allMovements = remember {
        listOf(
            HistoryMovement(1, "Hoy — 21 Sep 2026", "Hoy", "ENTRADA", "Azitromicina 500mg", "Carlos M. - 09:32", "+20", true, "Lote nuevo de proveedor."),
            HistoryMovement(2, "Hoy — 21 Sep 2026", "Hoy", "SALIDA", "Suero fisiológico", "Ana R. - 10:15", "-5", false, "Despacho a sala de atención."),
            HistoryMovement(3, "Hoy — 21 Sep 2026", "Hoy", "ENTRADA", "Jarabe pediátrico", "Carlos M. - 11:00", "+10", true, "Reposición de stock."),
            HistoryMovement(4, "Hoy — 21 Sep 2026", "Hoy", "SALIDA", "Ibuprofeno 400mg", "Luis G. - 12:30", "-4", false, "Venta directa de mostrador."),
            HistoryMovement(5, "Ayer — 20 Sep 2026", "Ayer", "ENTRADA", "Amoxicilina 250mg", "Ana R. - 15:20", "+30", true, "Ingreso de compra mensual."),
            HistoryMovement(6, "Ayer — 20 Sep 2026", "Ayer", "SALIDA", "Ibuprofeno 400mg", "Luis G. - 16:45", "-12", false, "Entrega de pedido #1042."),
            HistoryMovement(7, "Ayer — 20 Sep 2026", "Ayer", "SALIDA", "Paracetamol 500mg", "Carlos M. - 17:30", "-8", false, "Despacho regular a farmacia.")
        )
    }

    // Estados para los filtros con remember y mutableStateOf
    var selectedDate by remember { mutableStateOf("Todas") }       // "Todas", "Hoy", "Ayer"
    var selectedType by remember { mutableStateOf("Todos") }       // "Todos", "ENTRADA", "SALIDA"
    var selectedProduct by remember { mutableStateOf("Todos") }   // "Todos", nombre de producto

    // Menús desplegables para filtros
    var dateMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var productMenuExpanded by remember { mutableStateOf(false) }

    // Diálogo de detalle de movimiento
    var selectedMovementDetail by remember { mutableStateOf<HistoryMovement?>(null) }

    // Lista única de productos para el filtro
    val productOptions = remember {
        listOf("Todos") + allMovements.map { it.product }.distinct()
    }

    // Filtrar los movimientos según los criterios seleccionados
    val filteredMovements = remember(selectedDate, selectedType, selectedProduct) {
        allMovements.filter { item ->
            val matchDate = selectedDate == "Todas" || item.dayKey == selectedDate
            val matchType = selectedType == "Todos" || item.type.equals(selectedType, ignoreCase = true)
            val matchProduct = selectedProduct == "Todos" || item.product == selectedProduct
            matchDate && matchType && matchProduct
        }
    }

    // Agrupar movimientos filtrados por fecha
    val groupedMovements = remember(filteredMovements) {
        filteredMovements.groupBy { it.dateGroup }
    }

    val isAnyFilterActive = selectedDate != "Todas" || selectedType != "Todos" || selectedProduct != "Todos"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Historial de movimientos", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            "Registro completo de entradas y salidas",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (isAnyFilterActive) {
                        IconButton(onClick = {
                            selectedDate = "Todas"
                            selectedType = "Todos"
                            selectedProduct = "Todos"
                        }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar filtros")
                        }
                    }
                }
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
                    .widthIn(max = 840.dp)
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    // Filtro por Fecha
                    Box {
                        FilterChipButton(
                            label = if (selectedDate == "Todas") "Fecha" else selectedDate,
                            isActive = selectedDate != "Todas",
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
                                        selectedDate = dateOption
                                        dateMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Filtro por Tipo
                    Box {
                        FilterChipButton(
                            label = if (selectedType == "Todos") "Tipo" else if (selectedType == "ENTRADA") "Entradas" else "Salidas",
                            isActive = selectedType != "Todos",
                            onClick = { typeMenuExpanded = true }
                        )
                        DropdownMenu(
                            expanded = typeMenuExpanded,
                            onDismissRequest = { typeMenuExpanded = false }
                        ) {
                            listOf("Todos" to "Todos", "ENTRADA" to "Entradas", "SALIDA" to "Salidas").forEach { (typeVal, typeLabel) ->
                                DropdownMenuItem(
                                    text = { Text(typeLabel) },
                                    onClick = {
                                        selectedType = typeVal
                                        typeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Filtro por Producto
                    Box {
                        FilterChipButton(
                            label = if (selectedProduct == "Todos") "Producto" else selectedProduct.take(10) + "…",
                            isActive = selectedProduct != "Todos",
                            onClick = { productMenuExpanded = true }
                        )
                        DropdownMenu(
                            expanded = productMenuExpanded,
                            onDismissRequest = { productMenuExpanded = false }
                        ) {
                            productOptions.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text(prod) },
                                    onClick = {
                                        selectedProduct = prod
                                        productMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Si no hay resultados
                if (groupedMovements.isEmpty()) {
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
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Intenta modificando o limpiando los filtros seleccionados.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = {
                                    selectedDate = "Todas"
                                    selectedType = "Todos"
                                    selectedProduct = "Todos"
                                }
                            ) {
                                Text("Restablecer filtros")
                            }
                        }
                    }
                } else {
                    // Lista agrupada por fecha
                    groupedMovements.forEach { (dateHeader, movementsInGroup) ->
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
                                onClick = { selectedMovementDetail = movement }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal con detalles del movimiento al pulsar un elemento
    selectedMovementDetail?.let { mov ->
        AlertDialog(
            onDismissRequest = { selectedMovementDetail = null },
            title = { Text("Detalle del Movimiento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Producto: ${mov.product}", fontWeight = FontWeight.Bold)
                    Text("Tipo: ${mov.type} (${if (mov.isEntry) "Ingreso a almacén" else "Salida de almacén"})")
                    Text("Cantidad: ${mov.quantity} unidades")
                    Text("Fecha y Registro: ${mov.dateGroup} · ${mov.userTime}")
                    Text("Observación: ${mov.note}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMovementDetail = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun FilterChipButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
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
    movement: HistoryMovement,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(12.dp)
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
                // Etiqueta visual de Entrada/Salida
                Surface(
                    color = if (movement.isEntry) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = movement.type,
                        color = if (movement.isEntry) Color(0xFF2E7D32) else Color(0xFFE65100),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Column {
                    Text(
                        text = movement.product,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = movement.userTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = movement.quantity,
                fontWeight = FontWeight.Bold,
                color = if (movement.isEntry) Color(0xFF2E7D32) else Color(0xFFC62828),
                fontSize = 17.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}