package cl.teqmed.teqgastos.ui.rendicion

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RendicionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RendicionUiState())

    val uiState: StateFlow<RendicionUiState> = _uiState

    fun onTipoSeleccionado(tipo: String) {
        _uiState.value = _uiState.value.copy(
            tipoSeleccionado = tipo,
            errorTipo = null
        )
    }

    fun validarTipo(): Boolean {

        val tipoVacio = _uiState.value.tipoSeleccionado.isBlank()

        _uiState.value = _uiState.value.copy(
            errorTipo = if (tipoVacio)
                "Seleccione un tipo de rendición"
            else
                null
        )

        return !tipoVacio
    }
}
