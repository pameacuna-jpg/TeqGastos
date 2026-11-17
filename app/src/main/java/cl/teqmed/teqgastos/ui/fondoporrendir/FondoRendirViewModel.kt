package cl.teqmed.teqgastos.ui.fondorendir

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FondoRendirViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FondoRendirUiState())
    val uiState: StateFlow<FondoRendirUiState> = _uiState

    // Nombres de categorías activas con porcentaje mayor que cero.
    private var categoriasAutorizadas: Set<String> = emptySet()

    // Se mantiene el fondo individual asignado por Administración.
    fun actualizarFondoAsignado(monto: Long) {
        val montoSeguro = monto.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        _uiState.value = _uiState.value.copy(fondoAsignado = montoSeguro)
    }

    // Se llamará al cargar las asignaciones del trabajador desde Room.
    fun actualizarCategoriasAutorizadas(categorias: Set<String>) {
        categoriasAutorizadas = categorias
        val seleccionActual = _uiState.value.categoriaSeleccionada
        if (seleccionActual.isNotBlank() && seleccionActual !in categoriasAutorizadas) {
            _uiState.value = _uiState.value.copy(
                categoriaSeleccionada = "",
                errorCategoria = "La categoría seleccionada ya no está autorizada."
            )
        }
    }

    // Solo permite seleccionar categorías autorizadas.
    fun onCategoriaSeleccionada(categoria: String) {
        if (categoria !in categoriasAutorizadas) {
            _uiState.value = _uiState.value.copy(
                categoriaSeleccionada = "",
                errorCategoria = "Esta categoría no está autorizada para el trabajador."
            )
            return
        }
        _uiState.value = _uiState.value.copy(
            categoriaSeleccionada = categoria,
            errorCategoria = null
        )
    }

    // También valida la autorización antes de continuar al formulario de gasto.
    fun validarCategoria(): Boolean {
        val seleccion = _uiState.value.categoriaSeleccionada
        val error = when {
            seleccion.isBlank() -> "Seleccione una categoría"
            seleccion !in categoriasAutorizadas -> "Esta categoría no está autorizada para el trabajador."
            else -> null
        }
        _uiState.value = _uiState.value.copy(errorCategoria = error)
        return error == null
    }
}
