package com.inventtrack.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventtrack.InventTrackApplication
import com.inventtrack.ui.categories.CategoryViewModel
import com.inventtrack.ui.products.ProductFormViewModel
import com.inventtrack.ui.products.ProductListViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            ProductListViewModel(
                inventTrackApplication().container.productRepository,
                inventTrackApplication().container.categoryRepository
            )
        }
        initializer {
            ProductFormViewModel(
                this.createSavedStateHandle(),
                inventTrackApplication().container.productRepository
            )
        }
        initializer {
            CategoryViewModel(
                inventTrackApplication().container.categoryRepository
            )
        }
    }
}

fun CreationExtras.inventTrackApplication(): InventTrackApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as InventTrackApplication)
