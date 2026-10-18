package com.inventtrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.inventtrack.ui.components.ScreenContent
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementScreen() {
    val products = listOf("Paracetamol 500mg", "Ibuprofeno 400mg", "Azitromicina 500mg", "Suero fisiológico", "Jarabe pediátrico")
    var stocks by rememberSaveable { mutableStateOf(listOf(60, 18, 5, 45, 2)) }
    var type by rememberSaveable { mutableStateOf("Entrada") }
    var product by rememberSaveable { mutableStateOf(-1) }
    var quantity by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var note by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmation by rememberSaveable { mutableStateOf<String?>(null) }
    var menu by remember { mutableStateOf(false) }
    var calendar by rememberSaveable { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    ScreenContent("Registrar movimiento") {
        Text("Datos de ejemplo · los cambios se conservan solo durante esta demo.")
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf("Entrada", "Salida").forEachIndexed { index, label ->
                SegmentedButton(selected = type == label, onClick = { type = label; error = null },
                    shape = SegmentedButtonDefaults.itemShape(index, 2)) { Text(label) }
            }
        }
        ExposedDropdownMenuBox(expanded = menu, onExpandedChange = { menu = it }) {
            OutlinedTextField(value = products.getOrNull(product).orEmpty(), onValueChange = {}, readOnly = true,
                label = { Text("Producto") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menu) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth())
            ExposedDropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                products.forEachIndexed { index, name ->
                    DropdownMenuItem(text = { Text(name) }, onClick = { product = index; menu = false; error = null })
                }
            }
        }
        if (product >= 0) Text("Existencias: ${stocks[product]} unidades")
        OutlinedTextField(value = quantity, onValueChange = { quantity = it; error = null },
            label = { Text("Cantidad") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = { calendar = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Fecha: ${LocalDate.ofEpochDay(date).format(formatter)} · Cambiar")
        }
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Observación (opcional)") },
            minLines = 3, modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = {
            val amount = quantity.toIntOrNull()
            error = when {
                product !in products.indices -> "Selecciona un producto."
                amount == null || amount <= 0 -> "Ingresa una cantidad entera mayor que cero."
                type == "Salida" && amount > stocks[product] -> "La salida supera las existencias disponibles."
                type == "Entrada" && amount > Int.MAX_VALUE - stocks[product] -> "La cantidad supera el límite permitido."
                else -> null
            }
            if (error == null && amount != null) {
                stocks = stocks.mapIndexed { index, stock -> if (index == product) stock + if (type == "Entrada") amount else -amount else stock }
                confirmation = "$type de $amount unidades de ${products[product]} registrada en la demo el ${LocalDate.ofEpochDay(date).format(formatter)}." +
                    if (note.isNotBlank()) "\nObservación: ${note.trim()}" else ""
                quantity = ""
                note = ""
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Confirmar movimiento") }
    }
    confirmation?.let { message ->
        AlertDialog(onDismissRequest = { confirmation = null }, title = { Text("Movimiento registrado") },
            text = { Text(message) }, confirmButton = { TextButton(onClick = { confirmation = null }) { Text("Aceptar") } })
    }
    if (calendar) {
        val state = rememberDatePickerState(initialSelectedDateMillis = LocalDate.ofEpochDay(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { calendar = false }, confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay() }
                calendar = false
            }) { Text("Aceptar") }
        }, dismissButton = { TextButton(onClick = { calendar = false }) { Text("Cancelar") } }) { DatePicker(state) }
    }
}
