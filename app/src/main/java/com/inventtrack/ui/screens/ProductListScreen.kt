package com.inventtrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inventtrack.data.model.Producto

// Datos de ejemplo solicitados
val mockProductos = listOf(
    Producto(1, "Azitromicina 500 mg", "Antibióticos", "Caja con 3 tabletas", 150.0, 10, 5),
    Producto(2, "Suero fisiológico", "Insumos", "Botella 500ml", 45.0, 3, 10),
    Producto(3, "Jarabe pediátrico", "Jarabes", "Frasco 120ml", 80.0, 20, 5),
    Producto(4, "Paracetamol 500 mg", "Analgésicos", "Caja con 20 tabletas", 25.0, 50, 10)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Producto) -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    var listaProductos by remember { mutableStateOf(mockProductos) }

    val categorias = listOf("Todos", "Antibióticos", "Insumos", "Jarabes", "Analgésicos")

    val productosFiltrados = listaProductos.filter {
        (categoriaSeleccionada == "Todos" || it.categoria == categoriaSeleccionada) &&
        it.nombre.contains(busqueda, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Buscador
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar producto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chips de filtro por categoría
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categorias) { categoria ->
                    FilterChip(
                        selected = categoriaSeleccionada == categoria,
                        onClick = { categoriaSeleccionada = categoria },
                        label = { Text(categoria) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de productos
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(productosFiltrados) { producto ->
                    ProductCard(
                        producto = producto,
                        onEdit = { onNavigateToEdit(producto) },
                        onDelete = { productoAEliminar = producto }
                    )
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Estás seguro de que deseas eliminar ${producto.nombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    listaProductos = listaProductos.filter { it.id != producto.id }
                    productoAEliminar = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// -------------------------------------------------------------------------
// NOTA: Reemplazar los componentes debajo por los que haga Deny
// -------------------------------------------------------------------------

@Composable
fun ProductCard(producto: Producto, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = producto.nombre, style = MaterialTheme.typography.titleMedium)
                Text(text = producto.categoria, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(stockActual = producto.stockActual, stockMinimo = producto.stockMinimo)
            }
            Row {
                IconButton(onClick = onEdit) { 
                    Icon(Icons.Default.Edit, contentDescription = "Editar producto") 
                }
                IconButton(onClick = onDelete) { 
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar producto") 
                }
            }
        }
    }
}

@Composable
fun StatusChip(stockActual: Int, stockMinimo: Int) {
    val esStockBajo = stockActual <= stockMinimo
    val color = if (esStockBajo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val texto = if (esStockBajo) "Stock Bajo" else "Stock Normal"
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = texto,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}