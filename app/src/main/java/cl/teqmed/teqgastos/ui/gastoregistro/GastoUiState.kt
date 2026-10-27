package cl.teqmed.teqgastos.ui.gastoregistro

data class GastoUiState(

    // Información del gasto
    val tipoDocumento: String = "",
    val fechaDocumento: String = "",
    val monto: String = "",

    // Errores de validación
    val errorTipoDocumento: String? = null,
    val errorFechaDocumento: String? = null,
    val errorMonto: String? = null
)