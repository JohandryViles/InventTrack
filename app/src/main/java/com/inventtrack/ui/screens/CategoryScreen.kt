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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventtrack.data.Categoria
import com.inventtrack.ui.AppViewModelProvider
import com.inventtrack.ui.categories.CategoryUiState
import com.inventtrack.ui.categories.CategoryViewModel

@Composable
fun CategoryRoute(
    viewModel: CategoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CategoryScreen(
        uiState = uiState,
        onFormNombreChange = viewModel::onFormNombreChange,
        onFormDescripcionChange = viewModel::onFormDescripcionChange,
        onOpenAddForm = viewModel::openAddForm,
        onOpenEditForm = viewModel::openEditForm,
        onDismissForm = viewModel::dismissForm,
        onSaveForm = viewModel::saveCategory,
        onRequestDelete = viewModel::requestDelete,
        onDismissDelete = viewModel::dismissDelete,
        onConfirmDelete = viewModel::confirmDelete
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    uiState: CategoryUiState,
    onFormNombreChange: (String) -> Unit,
    onFormDescripcionChange: (String) -> Unit,
    onOpenAddForm: () -> Unit,
    onOpenEditForm: (Categoria) -> Unit,
    onDismissForm: () -> Unit,
    onSaveForm: () -> Unit,
    onRequestDelete: (Categoria) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Categorías") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenAddForm) {
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
            items(uiState.categorias) { categoria ->
                CategoryCard(
                    categoria = categoria,
                    onEdit = { onOpenEditForm(categoria) },
                    onDelete = { onRequestDelete(categoria) }
                )
            }
        }
    }

    if (uiState.categoriaEditando != null || uiState.formNombre.isNotEmpty() || uiState.formDescripcion.isNotEmpty()) {
        val isEditing = uiState.categoriaEditando != null
        AlertDialog(
            onDismissRequest = onDismissForm,
            title = { Text(if (isEditing) "Editar Categoría" else "Nueva Categoría") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.formNombre,
                        onValueChange = onFormNombreChange,
                        label = { Text("Nombre") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.formDescripcion,
                        onValueChange = onFormDescripcionChange,
                        label = { Text("Descripción") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onSaveForm, enabled = !uiState.isSaving && uiState.formNombre.isNotBlank()) {
                    Text(if (uiState.isSaving) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissForm, enabled = !uiState.isSaving) {
                    Text("Cancelar")
                }
            }
        )
    }

    uiState.categoriaAEliminar?.let { categoria ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Eliminar categoría") },
            text = { Text("¿Estás seguro de que deseas eliminar la categoría '${categoria.nombre}'?") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete, enabled = !uiState.isSaving) {
                    Text(if (uiState.isSaving) "Eliminando..." else "Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete, enabled = !uiState.isSaving) {
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
                if (categoria.descripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = categoria.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
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
