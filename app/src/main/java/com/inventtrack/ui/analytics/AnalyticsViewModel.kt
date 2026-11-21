package com.inventtrack.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.model.Movimiento
import com.inventtrack.data.model.Producto
import com.inventtrack.data.repository.MovementRepository
import com.inventtrack.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class AnalyticsPeriod(val title: String) { WEEK("Semanal"), MONTH("Mensual"), YEAR("Anual") }

data class CategoryAnalytics(val name: String, val percentage: Int, val rotation: String)

data class AnalyticsUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.WEEK,
    val labels: List<String> = emptyList(),
    val incoming: List<Int> = emptyList(),
    val outgoing: List<Int> = emptyList(),
    val totalIncoming: String = "0",
    val totalOutgoing: String = "0",
    val categories: List<CategoryAnalytics> = emptyList(),
    val dateRange: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class AnalyticsViewModel(
    movementRepository: MovementRepository,
    productRepository: ProductRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {
    private val period = MutableStateFlow(AnalyticsPeriod.WEEK)

    val uiState: StateFlow<AnalyticsUiState> = combine(
        movementRepository.observeMovements(), productRepository.observeProducts(), period
    ) { movements, products, selected -> Triple(movements, products, selected) }
        .map { (movements, products, selected) -> calculate(movements, products, selected) }
        .catch { emit(AnalyticsUiState(period = period.value, isLoading = false, error = "No se pudo cargar el análisis.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())

    fun onPeriodChange(selected: AnalyticsPeriod) { period.value = selected }

    private fun calculate(
        movements: List<Movimiento>, products: List<Producto>, selected: AnalyticsPeriod
    ): AnalyticsUiState {
        val today = LocalDate.now(clock)
        val start = when (selected) {
            AnalyticsPeriod.WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            AnalyticsPeriod.MONTH -> today.withDayOfMonth(1)
            AnalyticsPeriod.YEAR -> today.withDayOfYear(1)
        }
        val labels = when (selected) {
            AnalyticsPeriod.WEEK -> listOf("L", "M", "X", "J", "V", "S", "D")
            AnalyticsPeriod.MONTH -> (1..((today.lengthOfMonth() + 6) / 7)).map { "S$it" }
            AnalyticsPeriod.YEAR -> listOf("T1", "T2", "T3", "T4")
        }
        val filtered = movements.filter { it.fecha.toLocalDate() in start..today }
        fun bucket(movement: Movimiento): Int = when (selected) {
            AnalyticsPeriod.WEEK -> movement.fecha.dayOfWeek.value - 1
            AnalyticsPeriod.MONTH -> (movement.fecha.dayOfMonth - 1) / 7
            AnalyticsPeriod.YEAR -> (movement.fecha.monthValue - 1) / 3
        }
        val incoming = LongArray(labels.size)
        val outgoing = LongArray(labels.size)
        filtered.forEach { movement ->
            val series = if (movement.esEntrada) incoming else outgoing
            series[bucket(movement)] += movement.cantidad.toLong()
        }
        val productsById = products.associateBy { it.id }
        fun category(movement: Movimiento) = productsById[movement.productoId]?.categoria ?: "Sin categoría"
        val categoryNames = (products.map { it.categoria } + filtered.map(::category)).distinct().sorted()
        val exits = filtered.filterNot { it.esEntrada }.groupBy(::category)
            .mapValues { (_, list) -> list.sumOf { it.cantidad.toLong() } }
        val totalExits = outgoing.sum()
        // Mayor resto: los porcentajes enteros suman 100 incluso con tres categorías iguales.
        val percentages = categoryNames.associateWith { name ->
            if (totalExits == 0L) 0 else ((exits[name] ?: 0L) * 100.0 / totalExits).toInt()
        }.toMutableMap()
        if (totalExits > 0) {
            val remaining = 100 - percentages.values.sum()
            categoryNames.sortedByDescending { name ->
                (exits[name] ?: 0L) * 100.0 / totalExits - percentages.getValue(name)
            }.take(remaining).forEach { percentages[it] = percentages.getValue(it) + 1 }
        }
        val net = filtered.groupBy(::category).mapValues { (_, list) -> list.sumOf { it.delta.toLong() } }
        // El stock actual incluye registros con fecha futura; se descuentan para el cierre de hoy.
        val futureNet = movements.filter { it.fecha.toLocalDate() > today }.groupBy(::category)
            .mapValues { (_, list) -> list.sumOf { it.delta.toLong() } }
        val stock = products.groupBy { it.categoria }.mapValues { (_, list) -> list.sumOf { it.stockActual.toLong() } }
        val categories = categoryNames.map { name ->
            val closing = ((stock[name] ?: 0L) - (futureNet[name] ?: 0L)).coerceAtLeast(0L)
            val opening = (closing - (net[name] ?: 0L)).coerceAtLeast(0L)
            val average = (opening.toDouble() + closing.toDouble()) / 2
            val rotation = if (average == 0.0) "Sin base de stock" else
                String.format(Locale.forLanguageTag("es"), "%.2f veces", (exits[name] ?: 0L) / average)
            CategoryAnalytics(name, percentages.getValue(name), rotation)
        }
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return AnalyticsUiState(
            period = selected,
            labels = labels,
            incoming = incoming.map { it.coerceAtMost(Int.MAX_VALUE.toLong()).toInt() },
            outgoing = outgoing.map { it.coerceAtMost(Int.MAX_VALUE.toLong()).toInt() },
            totalIncoming = incoming.sum().toString(), totalOutgoing = totalExits.toString(),
            categories = categories,
            dateRange = "${start.format(formatter)} – ${today.format(formatter)}",
            isLoading = false
        )
    }
}
