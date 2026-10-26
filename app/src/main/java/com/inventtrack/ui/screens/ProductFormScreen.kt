package com.inventtrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventtrack.ui.AppViewModelProvider
import com.inventtrack.ui.products.ProductFormUiState
import com.inventtrack.ui.products.ProductFormViewModel

@Composable
fun ProductFormRoute(
    onNavigateBack: () -> Unit,
    viewModel: ProductFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect {
            onNavigateBack()
        }
    }

    ProductFormScreen(
        uiState = uiState,
        onNombreChange = viewModel::onNombreChange,
        onCategoriaChange = viewModel::onCategoriaChange,
        onDescripcionChange = viewModel::onDescripcionChange,
        onPrecioChange = viewModel::onPrecioChange,
        onStockActualChange = viewModel::onStockActualChange,
        onStockMinimoChange = viewModel::onStockMinimoChange,
        onSaveProduct = viewModel::saveProduct,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    uiState: ProductFormUiState,
    onNombreChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockActualChange: (String) -> Unit,
    onStockMinimoChange: (String) -> Unit,
    onSaveProduct: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.nombre.isNotBlank() && !uiState.nameError.isNullOrBlank()) "Editar Producto" else "Nuevo Producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre del producto") },
                isError = uiState.nameError != null,
                supportingText = { uiState.nameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.categoria,
                onValueChange = onCategoriaChange,
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = onDescripcionChange,
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = uiState.precio,
                onValueChange = onPrecioChange,
                label = { Text("Precio unitario") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = uiState.priceError != null,
                supportingText = { uiState.priceError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.stockActual,
                    onValueChange = onStockActualChange,
                    label = { Text("Stock actual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.stockError != null,
                    supportingText = { uiState.stockError?.let { Text(it) } },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = uiState.stockMinimo,
                    onValueChange = onStockMinimoChange,
                    label = { Text("Stock mínimo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSaveProduct,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Producto")
                }
            }
        }
    }
}
