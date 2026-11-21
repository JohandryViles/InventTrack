package com.inventtrack.ui.movements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventtrack.data.model.TipoMovimiento
import com.inventtrack.di.AppViewModelProvider
import com.inventtrack.ui.components.ScreenContent
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun MovementRoute(viewModel: MovementViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MovementScreen(
        uiState, viewModel::onTipoChange, viewModel::onProductoChange, viewModel::onCantidadChange,
        viewModel::onFechaChange, viewModel::onNotaChange, viewModel::registrar, viewModel::dismissConfirmation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementScreen(
    uiState: MovementUiState,
    onTipoChange: (TipoMovimiento) -> Unit,
    onProductoChange: (Int) -> Unit,
    onCantidadChange: (String) -> Unit,
    onFechaChange: (LocalDate) -> Unit,
    onNotaChange: (String) -> Unit,
    onRegistrar: () -> Unit,
    onDismissConfirmation: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    var calendar by rememberSaveable { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    ScreenContent("Registrar movimiento") {
        if (uiState.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            TipoMovimiento.entries.forEachIndexed { index, tipo ->
                SegmentedButton(selected = uiState.tipo == tipo, onClick = { onTipoChange(tipo) },
                    enabled = !uiState.isSaving,
                    shape = SegmentedButtonDefaults.itemShape(index, 2)) {
                    Text(if (tipo == TipoMovimiento.ENTRADA) "Entrada" else "Salida")
                }
            }
        }
        ExposedDropdownMenuBox(expanded = menu, onExpandedChange = { if (!uiState.isSaving) menu = it }) {
            OutlinedTextField(value = uiState.producto?.nombre.orEmpty(), onValueChange = {}, readOnly = true,
                enabled = !uiState.isSaving,
                label = { Text("Producto") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menu) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth())
            ExposedDropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                uiState.productos.forEach { product ->
                    DropdownMenuItem(text = { Text(product.nombre) }, onClick = { onProductoChange(product.id); menu = false })
                }
            }
        }
        if (!uiState.isLoading && uiState.productos.isEmpty()) Text("No hay productos disponibles.")
        uiState.producto?.let { Text("Existencias: ${it.stockActual} unidades") }
        OutlinedTextField(value = uiState.cantidad, onValueChange = onCantidadChange, enabled = !uiState.isSaving,
            label = { Text("Cantidad") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = { calendar = true }, enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()) {
            Text("Fecha: ${uiState.fecha.format(formatter)} · Cambiar")
        }
        OutlinedTextField(value = uiState.nota, onValueChange = onNotaChange, enabled = !uiState.isSaving, label = { Text("Observación (opcional)") },
            minLines = 3, modifier = Modifier.fillMaxWidth())
        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = onRegistrar, enabled = !uiState.isSaving && !uiState.isLoading && uiState.productos.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()) {
            if (uiState.isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Confirmar movimiento")
        }
    }
    uiState.confirmacion?.let { message ->
        AlertDialog(onDismissRequest = onDismissConfirmation, title = { Text("Movimiento registrado") },
            text = { Text(message) }, confirmButton = { TextButton(onClick = onDismissConfirmation) { Text("Aceptar") } })
    }
    if (calendar) {
        val state = rememberDatePickerState(initialSelectedDateMillis = uiState.fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { calendar = false }, confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onFechaChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                calendar = false
            }) { Text("Aceptar") }
        }, dismissButton = { TextButton(onClick = { calendar = false }) { Text("Cancelar") } }) { DatePicker(state) }
    }
}
