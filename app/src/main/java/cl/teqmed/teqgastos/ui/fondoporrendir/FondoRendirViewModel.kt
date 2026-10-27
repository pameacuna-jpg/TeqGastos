package cl.teqmed.teqgastos.ui.fondorendir

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FondoRendirViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FondoRendirUiState())

    val uiState: StateFlow<FondoRendirUiState> = _uiState

    fun onCategoriaSeleccionada(categoria: String) {
        _uiState.value = _uiState.value.copy(
            categoriaSeleccionada = categoria,
            errorCategoria = null
        )
    }

    fun validarCategoria(): Boolean {

        val categoriaVacia =
            _uiState.value.categoriaSeleccionada.isBlank()

        _uiState.value = _uiState.value.copy(
            errorCategoria = if (categoriaVacia)
                "Seleccione una categoría"
            else
                null
        )

        return !categoriaVacia
    }
}
