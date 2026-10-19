package com.inventtrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

// Modelo de datos temporal (Mock)
data class Categoria(
    val id: Int,
    val nombre: String,
    val cantidadProductos: Int
)

// Datos de ejemplo para categorías
val mockCategorias = listOf(
    Categoria(1, "Antibióticos", 15),
    Categoria(2, "Insumos", 40),
    Categoria(3, "Jarabes", 10),
    Categoria(4, "Analgésicos", 25)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen() {
    var listaCategorias by remember { mutableStateOf(mockCategorias) }
    var categoriaAEliminar by remember { mutableStateOf<Categoria?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Categorías") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Acción para agregar categoría (No especificada en el prompt, solo visual) */ }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva categoría")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaCategorias) { categoria ->
                CategoryCard(
                    categoria = categoria,
                    onEdit = { /* Acción para editar categoría */ },
                    onDelete = { categoriaAEliminar = categoria }
                )
            }
        }
    }

    // Diálogo de confirmación para eliminar categoría
    categoriaAEliminar?.let { categoria ->
        AlertDialog(
            onDismissRequest = { categoriaAEliminar = null },
            title = { Text("Eliminar categoría") },
            text = { Text("¿Estás seguro de que deseas eliminar la categoría '${categoria.nombre}'?") },
            confirmButton = {
                TextButton(onClick = {
                    listaCategorias = listaCategorias.filter { it.id != categoria.id }
                    categoriaAEliminar = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoriaAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CategoryCard(categoria: Categoria, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = categoria.nombre, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${categoria.cantidadProductos} productos", style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                IconButton(onClick = onEdit) { 
                    Icon(Icons.Default.Edit, contentDescription = "Editar categoría") 
                }
                IconButton(onClick = onDelete) { 
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar categoría") 
                }
            }
        }
    }
}