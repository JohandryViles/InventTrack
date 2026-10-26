package com.inventtrack.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventtrack.di.AppViewModelProvider
import com.inventtrack.ui.components.ScreenContent

@Composable
fun AnalyticsRoute(viewModel: AnalyticsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AnalyticsScreen(uiState, viewModel::onPeriodChange)
}

@Composable
fun AnalyticsScreen(uiState: AnalyticsUiState, onPeriodChange: (AnalyticsPeriod) -> Unit) {
    ScreenContent("Análisis y estadísticas") {
        TabRow(selectedTabIndex = uiState.period.ordinal) {
            AnalyticsPeriod.entries.forEach { period ->
                Tab(selected = uiState.period == period, onClick = { onPeriodChange(period) }, text = { Text(period.title) })
            }
        }
        if (uiState.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (!uiState.isLoading && uiState.error == null) {
            Text(uiState.dateRange)
            Text("${uiState.totalIncoming} entradas · ${uiState.totalOutgoing} salidas", style = MaterialTheme.typography.titleMedium)
            MovementBarChart(uiState.labels, uiState.incoming, uiState.outgoing)
            CategoryDonut(uiState.categories.map { it.name }, uiState.categories.map { it.percentage })
            Text("Rotación de inventario", style = MaterialTheme.typography.titleLarge)
            Text("Salidas del período / promedio del stock inicial estimado y actual. El stock inicial se estima restando las entradas netas del período al stock actual.")
            uiState.categories.forEach { RotationCard(it.name, it.rotation, Modifier.fillMaxWidth()) }
        }
    }
}

@Composable
fun MovementBarChart(labels: List<String>, incoming: List<Int>, outgoing: List<Int>) {
    val entryColor = MaterialTheme.colorScheme.primary
    val exitColor = MaterialTheme.colorScheme.secondary
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Entradas y salidas · unidades", style = MaterialTheme.typography.titleLarge)
            Text("Entradas", color = entryColor)
            Text("Salidas", color = exitColor)
            val maximum = maxOf(incoming.maxOrNull() ?: 0, outgoing.maxOrNull() ?: 0, 1).toFloat()
            Text("Escala: 0–${maximum.toInt()} unidades", style = MaterialTheme.typography.labelMedium)
            val description = labels.indices.joinToString("; ") { "${labels[it]}: ${incoming.getOrElse(it) { 0 }} entradas, ${outgoing.getOrElse(it) { 0 }} salidas" }
            Canvas(Modifier.fillMaxWidth().height(190.dp).semantics { contentDescription = description }) {
                val group = size.width / labels.size.coerceAtLeast(1)
                val width = group * .28f
                drawLine(Color.Gray, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                labels.indices.forEach { i ->
                    val x = group * i + group * .16f
                    val h1 = incoming.getOrElse(i) { 0 } / maximum * size.height
                    val h2 = outgoing.getOrElse(i) { 0 } / maximum * size.height
                    drawRect(entryColor, Offset(x, size.height - h1), Size(width, h1))
                    drawRect(exitColor, Offset(x + width + group * .06f, size.height - h2), Size(width, h2))
                }
            }
            Row(Modifier.fillMaxWidth()) {
                labels.forEach { Text(it, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            }
            Text(labels.indices.joinToString(" · ") { "${labels[it]}: ${incoming.getOrElse(it) { 0 }}/${outgoing.getOrElse(it) { 0 }}" }, style = MaterialTheme.typography.bodySmall)
            if (incoming.all { it == 0 } && outgoing.all { it == 0 }) Text("Sin movimientos en este período.")
        }
    }
}

@Composable
fun CategoryDonut(categoryNames: List<String>, shares: List<Int>) {
    val categoryColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary,
        Color(0xFF6489C5), Color(0xFFBA6B35), Color(0xFF9270B0), Color(0xFF578148))
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Salidas por categoría", style = MaterialTheme.typography.titleLarge)
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(200.dp).semantics {
                    contentDescription = categoryNames.indices.joinToString { "${categoryNames[it]} ${shares.getOrElse(it) { 0 }} por ciento" }
                }) {
                    val stroke = 32.dp.toPx()
                    var start = -90f
                    if (shares.all { it == 0 }) drawArc(Color.LightGray, 0f, 360f, false,
                        Offset(stroke / 2, stroke / 2), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke))
                    shares.forEachIndexed { i, share ->
                        val sweep = share * 3.6f
                        drawArc(categoryColors[i % categoryColors.size], start, sweep, false,
                            Offset(stroke / 2, stroke / 2), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke))
                        start += sweep
                    }
                }
                Text("${shares.sum()}%", style = MaterialTheme.typography.headlineMedium)
            }
            categoryNames.forEachIndexed { i, name -> Text("$name · ${shares.getOrElse(i) { 0 }}%", color = categoryColors[i % categoryColors.size]) }
            if (shares.all { it == 0 }) Text("Sin salidas en este período.")
        }
    }
}

@Composable
fun RotationCard(name: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
