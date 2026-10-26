package com.inventtrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inventtrack.data.model.Producto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productoAEditar: Producto? = null,
    onNavigateBack: () -> Unit,
    onSave: (Producto) -> Unit
) {
    // Estados locales para el formulario
    var nombre by remember { mutableStateOf(productoAEditar?.nombre ?: "") }
    var categoria by remember { mutableStateOf(productoAEditar?.categoria ?: "") }
    var descripcion by remember { mutableStateOf(productoAEditar?.descripcion ?: "") }
    var precio by remember { mutableStateOf(productoAEditar?.precio?.toString() ?: "") }
    var stockActual by remember { mutableStateOf(productoAEditar?.stockActual?.toString() ?: "") }
    var stockMinimo by remember { mutableStateOf(productoAEditar?.stockMinimo?.toString() ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productoAEditar == null) "Crear Producto" else "Editar Producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = stockActual,
                    onValueChange = { stockActual = it },
                    label = { Text("Stock Actual") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = stockMinimo,
                    onValueChange = { stockMinimo = it },
                    label = { Text("Stock Mínimo") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = {
                        val nuevoProducto = Producto(
                            id = productoAEditar?.id ?: (1000..9999).random(),
                            nombre = nombre,
                            categoria = categoria,
                            descripcion = descripcion,
                            precio = precio.toDoubleOrNull() ?: 0.0,
                            stockActual = stockActual.toIntOrNull() ?: 0,
                            stockMinimo = stockMinimo.toIntOrNull() ?: 0
                        )
                        onSave(nuevoProducto)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}