package com.inventtrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inventtrack.ui.components.*

@Composable
fun ProductsScreen(onNewProduct: () -> Unit, onCategories: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    ScreenContent("Productos") {
        InventTrackTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "Buscar producto...",
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrimaryButton(
                text = "+ Nuevo Producto",
                onClick = onNewProduct,
                modifier = Modifier.weight(1f)
            )
            OutlinedSecondaryButton(
                text = "Categorías",
                onClick = onCategories,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ProductCard(name = "Paracetamol 500mg", stock = 60, level = StockLevel.DISPONIBLE)
        ProductCard(name = "Ibuprofeno 400mg", stock = 18, level = StockLevel.DISPONIBLE)
        ProductCard(name = "Azitromicina 500mg", stock = 5, level = StockLevel.BAJO)
        ProductCard(name = "Suero fisiológico", stock = 45, level = StockLevel.DISPONIBLE)
        ProductCard(name = "Jarabe pediátrico", stock = 2, level = StockLevel.CRITICO)
    }
}
