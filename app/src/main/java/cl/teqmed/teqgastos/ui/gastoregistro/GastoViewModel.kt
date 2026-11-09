
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

    // Tipo de documento
    fun onTipoDocumentoSeleccionado(tipo: String) {
        _uiState.value = _uiState.value.copy(
            tipoDocumento = tipo,
            errorTipoDocumento = null
        )
    }

    // Número de documento
    fun onNumeroDocumentoChange(numero: String) {
        _uiState.value = _uiState.value.copy(
            numeroDocumento = numero,
            errorNumeroDocumento = null
        )
    }

    // RUT del proveedor
    fun onRutEmisorChange(rut: String) {
        _uiState.value = _uiState.value.copy(
            rutEmisor = rut,
            errorRutEmisor = null
        )
    }

    // Nombre del proveedor
    fun onNombreProveedorChange(nombre: String) {
        _uiState.value = _uiState.value.copy(
            nombreProveedor = nombre,
            errorNombreProveedor = null
        )
    }

    // Fecha del documento
    fun onFechaDocumentoChange(fecha: String) {
        _uiState.value = _uiState.value.copy(
            fechaDocumento = fecha,
            errorFechaDocumento = null
        )
    }

    // Monto del gasto
    fun onMontoChange(monto: String) {
        _uiState.value = _uiState.value.copy(
            monto = monto,
            errorMonto = null
        )
    }

    // Motivo individual del reembolso
    fun onMotivoReembolsoChange(motivo: String) {
        _uiState.value = _uiState.value.copy(
            motivoReembolso = motivo,
            errorMotivoReembolso = null
        )
    }

    // NUEVO: cargar los datos de un gasto para editarlo
    fun cargarGasto(gasto: GastoUiState) {
        _uiState.value = gasto.copy(
            errorTipoDocumento = null,
            errorNumeroDocumento = null,
            errorRutEmisor = null,
            errorNombreProveedor = null,
            errorFechaDocumento = null,
            errorMonto = null,
            errorMotivoReembolso = null
        )
    }

    // Validar formulario
    // esReembolso indica si el motivo es obligatorio
    fun validarGasto(esReembolso: Boolean = false): Boolean {

        val estado = _uiState.value

        val errorTipoDocumento =
            if (
                estado.tipoDocumento != "Boleta" &&
                estado.tipoDocumento != "Factura"
            ) {
                "Seleccione Boleta o Factura"
            } else {
                null
            }

        val numero = estado.numeroDocumento.trim()

        val errorNumeroDocumento = when {
            numero.isBlank() ->
                "Ingrese el número del documento"

            numero.length > 30 ->
                "El número del documento es demasiado largo"

            !numero.matches(Regex("[0-9A-Za-z-]+")) ->
                "Ingrese un número de documento válido"

            else -> null
        }

        val errorRutEmisor = when {
            estado.rutEmisor.isBlank() ->
                "Ingrese el RUT del proveedor"

            !validarRutChileno(estado.rutEmisor) ->
                "El RUT del proveedor no es válido"

            else -> null
        }

        val nombre = estado.nombreProveedor.trim()

        val errorNombreProveedor = when {
            nombre.isBlank() ->
                "Ingrese el nombre del proveedor"

            nombre.length > 120 ->
                "El nombre del proveedor es demasiado largo"

            else -> null
        }

        val errorFechaDocumento = validarFechaDocumento()

        val montoNumero = estado.monto.toDoubleOrNull()

        val errorMonto = when {
            estado.monto.isBlank() ->
                "Ingrese el monto"

            montoNumero == null || !montoNumero.isFinite() ->
                "Ingrese un monto válido"

            montoNumero <= 0 ->
                "El monto debe ser mayor a 0"

            else -> null
        }

        // Solo se exige motivo para reembolsos
        val motivo = estado.motivoReembolso.trim()

        val errorMotivoReembolso = when {
            !esReembolso -> null

            motivo.isBlank() ->
                "Explique el motivo de este reembolso"

            motivo.length > 500 ->
                "El motivo no puede superar 500 caracteres"

            else -> null
        }

        _uiState.value = estado.copy(
            errorTipoDocumento = errorTipoDocumento,
            errorNumeroDocumento = errorNumeroDocumento,
            errorRutEmisor = errorRutEmisor,
            errorNombreProveedor = errorNombreProveedor,
            errorFechaDocumento = errorFechaDocumento,
            errorMonto = errorMonto,
            errorMotivoReembolso = errorMotivoReembolso
        )

        return errorTipoDocumento == null &&
                errorNumeroDocumento == null &&
                errorRutEmisor == null &&
                errorNombreProveedor == null &&
                errorFechaDocumento == null &&
                errorMonto == null &&
                errorMotivoReembolso == null
    }

    // Limpiar formulario para registrar otro gasto
    fun limpiarFormulario() {
        _uiState.value = GastoUiState()
    }

    // Validar fecha del documento
    private fun validarFechaDocumento(): String? {

        val fechaTexto = _uiState.value.fechaDocumento

        if (fechaTexto.isBlank()) {
            return "Seleccione la fecha del documento"
        }

        val formato = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )

        formato.isLenient = false

        val fechaSeleccionada = try {
            formato.parse(fechaTexto)
        } catch (e: Exception) {
            null
        }

        if (fechaSeleccionada == null) {
            return "Fecha del documento no válida"
        }

        val hoy = Calendar.getInstance()

        hoy.set(Calendar.HOUR_OF_DAY, 0)
        hoy.set(Calendar.MINUTE, 0)
        hoy.set(Calendar.SECOND, 0)
        hoy.set(Calendar.MILLISECOND, 0)

        if (fechaSeleccionada.after(hoy.time)) {
            return "La fecha del documento no puede ser futura"
        }

        val hace30Dias = hoy.clone() as Calendar
        hace30Dias.add(Calendar.DAY_OF_YEAR, -30)

        if (fechaSeleccionada.before(hace30Dias.time)) {
            return "La fecha del documento no puede superar los 30 días"
        }

        return null
    }

    // Validar RUT chileno mediante módulo 11
    private fun validarRutChileno(rut: String): Boolean {

        val rutLimpio = rut.uppercase(Locale.ROOT)
            .replace(".", "")
            .replace("-", "")
            .replace(" ", "")

        if (!rutLimpio.matches(Regex("[0-9]{7,8}[0-9K]"))) {
            return false
        }

        val cuerpo = rutLimpio.dropLast(1)
        val digitoIngresado = rutLimpio.last()

        var suma = 0
        var multiplicador = 2

        for (digito in cuerpo.reversed()) {

            suma += (digito - '0') * multiplicador

            multiplicador++

            if (multiplicador > 7) {
                multiplicador = 2
            }
        }

        val resultado = 11 - (suma % 11)

        val digitoCalculado = when (resultado) {
            11 -> '0'
            10 -> 'K'
            else -> ('0' + resultado)
        }

        return digitoIngresado == digitoCalculado
    }
}
