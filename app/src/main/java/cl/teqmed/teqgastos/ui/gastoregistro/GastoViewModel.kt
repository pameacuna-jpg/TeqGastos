package cl.teqmed.teqgastos.ui.gastoregistro

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GastoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GastoUiState())

    val uiState: StateFlow<GastoUiState> = _uiState

    fun onFechaChange(fecha: String) {
        _uiState.value = _uiState.value.copy(
            fecha = fecha,
            errorFecha = null
        )
    }

    fun onMontoChange(monto: String) {
        _uiState.value = _uiState.value.copy(
            monto = monto,
            errorMonto = null
        )
    }

    fun validarGasto(): Boolean {

        val fechaVacia = _uiState.value.fecha.isBlank()
        val montoVacio = _uiState.value.monto.isBlank()

        _uiState.value = _uiState.value.copy(
            errorFecha = if (fechaVacia)
                "Ingrese la fecha"
            else
                null,

            errorMonto = if (montoVacio)
                "Ingrese el monto"
            else
                null
        )

        return !fechaVacia && !montoVacio
    }
}
