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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventtrack.data.Producto
import com.inventtrack.ui.AppViewModelProvider
import com.inventtrack.ui.products.ProductListUiState
import com.inventtrack.ui.products.ProductListViewModel

@Composable
fun ProductListRoute(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: ProductListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProductListScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onCategoryChange = viewModel::onCategoryChange,
        onNavigateToAdd = onNavigateToAdd,
        onNavigateToEdit = onNavigateToEdit,
        onDeleteRequest = viewModel::onDeleteRequest,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDelete = { viewModel.onDeleteRequest(null) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    uiState: ProductListUiState,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onDeleteRequest: (Producto) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit
) {
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
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                label = { Text("Buscar producto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.categorias) { categoria ->
                    FilterChip(
                        selected = uiState.categoriaSeleccionada == categoria,
                        onClick = { onCategoryChange(categoria) },
                        label = { Text(categoria) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.productos) { producto ->
                        ProductCard(
                            producto = producto,
                            onEdit = { onNavigateToEdit(producto.id) },
                            onDelete = { onDeleteRequest(producto) }
                        )
                    }
                }
            }
        }
    }

    uiState.productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Eliminar producto") },
            text = { Text("¿Estás seguro de que deseas eliminar ${producto.nombre}?") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text("Cancelar")
                }
            }
        )
    }
}

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
