package com.inventtrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inventtrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventTrackTopBar(title: String) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold, color = Color.White) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryDarkBlue)
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 400.dp) // Adaptabilidad
            .height(50.dp)
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OutlinedSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 400.dp)
            .height(50.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventTrackTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 400.dp),
        shape = Shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SecondaryTeal,
            focusedLabelColor = SecondaryTeal
        )
    )
}

enum class StockLevel { DISPONIBLE, BAJO, CRITICO }

@Composable
fun StatusChip(level: StockLevel) {
    val (backgroundColor, text) = when (level) {
        StockLevel.DISPONIBLE -> Pair(AvailableGreen, "Disponible")
        StockLevel.BAJO -> Pair(LowOrange, "Bajo")
        StockLevel.CRITICO -> Pair(CriticalRed, "Crítico")
    }

    Box(
        modifier = Modifier
            .background(color = backgroundColor.copy(alpha = 0.2f), shape = Shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = backgroundColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProductCard(name: String, stock: Int, level: StockLevel) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .padding(vertical = 8.dp),
        shape = Shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Stock: $stock unidades", style = MaterialTheme.typography.bodyMedium)
            }
            StatusChip(level = level)
        }
    }
}