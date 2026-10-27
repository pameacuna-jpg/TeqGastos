package cl.teqmed.teqgastos.ui.proyecto

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProyectoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProyectoUiState())

    val uiState: StateFlow<ProyectoUiState> = _uiState

    fun onProyectoSeleccionado(proyecto: String) {
        _uiState.value = _uiState.value.copy(
            proyectoSeleccionado = proyecto,
            errorProyecto = null
        )
    }

    fun validarProyecto(): Boolean {

        val proyectoVacio = _uiState.value.proyectoSeleccionado.isBlank()

        _uiState.value = _uiState.value.copy(
            errorProyecto = if (proyectoVacio)
                "Seleccione un proyecto"
            else
                null
        )

        return !proyectoVacio
    }
}