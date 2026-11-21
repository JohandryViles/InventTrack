package com.inventtrack.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventtrack.data.model.Categoria
import com.inventtrack.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categorias: List<Categoria> = emptyList(),
    val isSaving: Boolean = false,
    val isFormVisible: Boolean = false,
    val categoriaEditando: Categoria? = null,
    val categoriaAEliminar: Categoria? = null,
    val formNombre: String = "",
    val formDescripcion: String = ""
)

class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    private val _isFormVisible = MutableStateFlow(false)
    private val _categoriaEditando = MutableStateFlow<Categoria?>(null)
    private val _categoriaAEliminar = MutableStateFlow<Categoria?>(null)
    private val _formNombre = MutableStateFlow("")
    private val _formDescripcion = MutableStateFlow("")

    private data class InternalState(
        val isSaving: Boolean,
        val isFormVisible: Boolean,
        val categoriaEditando: Categoria?,
        val categoriaAEliminar: Categoria?,
        val formNombre: String,
        val formDescripcion: String
    )

    private val _accionesState = combine(
        _isSaving, _categoriaEditando, _categoriaAEliminar
    ) { saving, editando, eliminar -> Triple(saving, editando, eliminar) }

    private val _formState = combine(
        _isFormVisible, _formNombre, _formDescripcion
    ) { visible, nombre, descripcion -> Triple(visible, nombre, descripcion) }

    private val _internalState = combine(_accionesState, _formState) { acciones, form ->
        InternalState(
            isSaving = acciones.first,
            isFormVisible = form.first,
            categoriaEditando = acciones.second,
            categoriaAEliminar = acciones.third,
            formNombre = form.second,
            formDescripcion = form.third
        )
    }

    val uiState: StateFlow<CategoryUiState> = combine(
        categoryRepository.observeCategories(),
        _internalState
    ) { categorias, state ->
        CategoryUiState(
            categorias = categorias.filter { it.nombre != "Todos" }, // Omitir la de filtro si existiera
            isSaving = state.isSaving,
            isFormVisible = state.isFormVisible,
            categoriaEditando = state.categoriaEditando,
            categoriaAEliminar = state.categoriaAEliminar,
            formNombre = state.formNombre,
            formDescripcion = state.formDescripcion
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryUiState()
    )

    fun onFormNombreChange(nombre: String) {
        _formNombre.value = nombre
    }

    fun onFormDescripcionChange(descripcion: String) {
        _formDescripcion.value = descripcion
    }

    fun openAddForm() {
        _categoriaEditando.value = null
        _formNombre.value = ""
        _formDescripcion.value = ""
        _isFormVisible.value = true
    }

    fun openEditForm(categoria: Categoria) {
        _categoriaEditando.value = categoria
        _formNombre.value = categoria.nombre
        _formDescripcion.value = categoria.descripcion
        _isFormVisible.value = true
    }

    fun dismissForm() {
        _categoriaEditando.value = null
        _formNombre.value = ""
        _formDescripcion.value = ""
        _isFormVisible.value = false
    }

    fun requestDelete(categoria: Categoria) {
        _categoriaAEliminar.value = categoria
    }

    fun dismissDelete() {
        _categoriaAEliminar.value = null
    }

    fun confirmDelete() {
        val cat = _categoriaAEliminar.value ?: return
        viewModelScope.launch {
            _isSaving.update { true }
            categoryRepository.delete(cat.id)
            _categoriaAEliminar.value = null
            _isSaving.update { false }
        }
    }

    fun saveCategory() {
        if (_formNombre.value.isBlank()) return
        viewModelScope.launch {
            _isSaving.update { true }
            val edit = _categoriaEditando.value
            val categoria = if (edit != null) {
                edit.copy(nombre = _formNombre.value, descripcion = _formDescripcion.value)
            } else {
                Categoria(
                    id = 0,
                    nombre = _formNombre.value,
                    descripcion = _formDescripcion.value
                )
            }
            categoryRepository.upsert(categoria)
            _categoriaEditando.value = null
            _formNombre.value = ""
            _formDescripcion.value = ""
            _isFormVisible.value = false
            _isSaving.update { false }
        }
    }
}
