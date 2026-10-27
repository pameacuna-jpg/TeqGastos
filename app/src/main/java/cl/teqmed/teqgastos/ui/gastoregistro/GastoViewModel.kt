package cl.teqmed.teqgastos.ui.gastoregistro

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GastoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GastoUiState())

    val uiState: StateFlow<GastoUiState> = _uiState

    fun onTipoDocumentoSeleccionado(tipo: String) {

        _uiState.value = _uiState.value.copy(
            tipoDocumento = tipo,
            errorTipoDocumento = null
        )
    }

    fun onFechaDocumentoChange(fecha: String) {

        _uiState.value = _uiState.value.copy(
            fechaDocumento = fecha,
            errorFechaDocumento = null
        )
    }

    fun onMontoChange(monto: String) {

        _uiState.value = _uiState.value.copy(
            monto = monto,
            errorMonto = null
        )
    }

    fun validarGasto(): Boolean {

        val errorTipoDocumento =
            if (_uiState.value.tipoDocumento.isBlank()) {
                "Seleccione Boleta o Factura"
            } else {
                null
            }

        val errorFechaDocumento =
            validarFechaDocumento()

        val montoNumero =
            _uiState.value.monto.toDoubleOrNull()

        val errorMonto = when {

            _uiState.value.monto.isBlank() ->
                "Ingrese el monto"

            montoNumero == null ->
                "Ingrese un monto válido"

            montoNumero <= 0 ->
                "El monto debe ser mayor a 0"

            else -> null
        }

        _uiState.value = _uiState.value.copy(
            errorTipoDocumento = errorTipoDocumento,
            errorFechaDocumento = errorFechaDocumento,
            errorMonto = errorMonto
        )

        return errorTipoDocumento == null &&
                errorFechaDocumento == null &&
                errorMonto == null
    }

    private fun validarFechaDocumento(): String? {

        if (_uiState.value.fechaDocumento.isBlank()) {
            return "Seleccione la fecha del documento"
        }

        val formato = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )

        formato.isLenient = false

        val fechaSeleccionada =
            try {
                formato.parse(
                    _uiState.value.fechaDocumento
                )
            } catch (e: Exception) {
                null
            }

        if (fechaSeleccionada == null) {
            return "Fecha del documento no válida"
        }

        val hoy = Calendar.getInstance()

        hoy.set(Calendar.HOUR_OF_DAY, 23)
        hoy.set(Calendar.MINUTE, 59)
        hoy.set(Calendar.SECOND, 59)

        if (fechaSeleccionada.after(hoy.time)) {
            return "La fecha del documento no puede ser futura"
        }

        val hace30Dias = Calendar.getInstance()

        hace30Dias.add(
            Calendar.DAY_OF_YEAR,
            -30
        )

        if (fechaSeleccionada.before(hace30Dias.time)) {
            return "La fecha del documento no puede superar los 30 días"
        }

        return null
    }
}