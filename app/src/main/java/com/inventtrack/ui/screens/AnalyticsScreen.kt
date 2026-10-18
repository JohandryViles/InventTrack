package com.inventtrack.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.inventtrack.ui.components.ScreenContent

@Composable
fun AnalyticsScreen() {
    var period by rememberSaveable { mutableStateOf(0) }
    val periods = listOf("Semanal", "Mensual", "Anual")
    val labels = when (period) {
        0 -> listOf("L", "M", "X", "J", "V", "S", "D")
        1 -> listOf("S1", "S2", "S3", "S4")
        else -> listOf("T1", "T2", "T3", "T4")
    }
    val incoming = when (period) { 0 -> listOf(20, 35, 18, 42, 30, 15, 25); 1 -> listOf(140, 185, 160, 210); else -> listOf(1600, 2100, 1800, 2400) }
    val outgoing = when (period) { 0 -> listOf(12, 22, 15, 28, 24, 10, 17); 1 -> listOf(110, 140, 125, 175); else -> listOf(1250, 1700, 1450, 2000) }
    val shares = when (period) { 0 -> listOf(50, 30, 20); 1 -> listOf(40, 35, 25); else -> listOf(45, 25, 30) }
    val entryColor = MaterialTheme.colorScheme.primary
    val exitColor = MaterialTheme.colorScheme.secondary
    val categoryColors = listOf(entryColor, exitColor, Color(0xFF6489C5))
    val categoryNames = listOf("Analgésicos", "Antibióticos", "Jarabes")
    ScreenContent("Análisis y estadísticas") {
        Text("Datos de ejemplo independientes del formulario de movimientos.")
        TabRow(selectedTabIndex = period) {
            periods.forEachIndexed { index, title -> Tab(selected = period == index, onClick = { period = index }, text = { Text(title) }) }
        }
        Text("${incoming.sum()} entradas · ${outgoing.sum()} salidas", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Entradas y salidas · unidades", style = MaterialTheme.typography.titleLarge)
                Text("Entradas", color = entryColor)
                Text("Salidas", color = exitColor)
                val maximum = maxOf(incoming.max(), outgoing.max()).toFloat()
                Text("Escala: 0–${maximum.toInt()} unidades", style = MaterialTheme.typography.labelMedium)
                val description = labels.indices.joinToString("; ") { "${labels[it]}: ${incoming[it]} entradas, ${outgoing[it]} salidas" }
                Canvas(Modifier.fillMaxWidth().height(190.dp).semantics { contentDescription = description }) {
                    val group = size.width / labels.size
                    val width = group * .28f
                    drawLine(Color.Gray, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                    incoming.indices.forEach { i ->
                        val x = group * i + group * .16f
                        val h1 = incoming[i] / maximum * size.height
                        val h2 = outgoing[i] / maximum * size.height
                        drawRect(entryColor, Offset(x, size.height - h1), Size(width, h1))
                        drawRect(exitColor, Offset(x + width + group * .06f, size.height - h2), Size(width, h2))
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    labels.forEach { Text(it, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
                }
                Text(labels.indices.joinToString(" · ") { "${labels[it]}: ${incoming[it]}/${outgoing[it]}" }, style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Salidas por categoría", style = MaterialTheme.typography.titleLarge)
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(200.dp).semantics {
                        contentDescription = categoryNames.indices.joinToString { "${categoryNames[it]} ${shares[it]} por ciento" }
                    }) {
                        val stroke = 32.dp.toPx()
                        var start = -90f
                        shares.forEachIndexed { i, share ->
                            val sweep = share * 3.6f
                            drawArc(categoryColors[i], start, sweep, false,
                                Offset(stroke / 2, stroke / 2), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke))
                            start += sweep
                        }
                    }
                    Text("100%", style = MaterialTheme.typography.headlineMedium)
                }
                categoryNames.forEachIndexed { i, name -> Text("$name · ${shares[i]}%", color = categoryColors[i]) }
            }
        }
        Text("Rotación de inventario", style = MaterialTheme.typography.titleLarge)
        Text("Salidas del período / inventario promedio (mock).")
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val values = listOf("Analgésicos" to listOf("1,8", "3,2", "12,4")[period],
                "Antibióticos" to listOf("2,4", "4,1", "16,8")[period], "Jarabes" to listOf("0,9", "1,7", "7,2")[period])
            if (maxWidth < 520.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { values.forEach { RotationCard(it.first, it.second, Modifier.fillMaxWidth()) } }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { values.forEach { RotationCard(it.first, it.second, Modifier.weight(1f)) } }
            }
        }
    }
}

@Composable
private fun RotationCard(name: String, value: String, modifier: Modifier) {
    ElevatedCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text("$value veces", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
