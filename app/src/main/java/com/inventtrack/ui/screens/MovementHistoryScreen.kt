package com.inventtrack.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementHistoryScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Historial de movimientos", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Registro completo de entradas y salidas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Filtros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipWidget(label = "Fecha", selected = true)
                FilterChipWidget(label = "Producto", selected = false)
                FilterChipWidget(label = "Tipo", selected = false)
            }

            // Agrupación: Hoy
            Text(
                text = "Hoy — 21 Sep 2026",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            HistoryItem(type = "ENTRADA", product = "Azitromicina 500mg", userTime = "Carlos M. - 09:32", quantity = "+20", isEntry = true)
            HistoryItem(type = "SALIDA", product = "Suero fisiológico", userTime = "Ana R. - 10:15", quantity = "-5", isEntry = false)
            HistoryItem(type = "ENTRADA", product = "Jarabe pediátrico", userTime = "Carlos M. - 11:00", quantity = "+10", isEntry = true)

            Spacer(modifier = Modifier.height(16.dp))

            // Agrupación: Ayer
            Text(
                text = "Ayer — 20 Sep 2026",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            HistoryItem(type = "ENTRADA", product = "Amoxicilina 250mg", userTime = "Ana R. - 15:20", quantity = "+30", isEntry = true)
            HistoryItem(type = "SALIDA", product = "Ibuprofeno 400mg", userTime = "Luis G. - 16:45", quantity = "-12", isEntry = false)
            HistoryItem(type = "SALIDA", product = "Paracetamol 500mg", userTime = "Carlos M. - 17:30", quantity = "-8", isEntry = false)
        }
    }
}

@Composable
fun FilterChipWidget(label: String, selected: Boolean) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(text = label, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun HistoryItem(type: String, product: String, userTime: String, quantity: String, isEntry: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Etiqueta visual de Entrada/Salida
                Surface(
                    color = if (isEntry) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = type,
                        color = if (isEntry) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
                Column {
                    Text(text = product, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = userTime, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Text(
                text = quantity,
                fontWeight = FontWeight.Bold,
                color = if (isEntry) Color(0xFF4CAF50) else Color.Red,
                fontSize = 18.sp
            )
        }
    }
}