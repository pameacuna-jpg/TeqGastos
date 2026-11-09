package cl.teqmed.teqgastos.ui.gastoregistro

data class GastoUiState(

    // Información del documento y del gasto
    val tipoDocumento: String = "",
    val numeroDocumento: String = "",
    val rutEmisor: String = "",
    val nombreProveedor: String = "",
    val fechaDocumento: String = "",
    val monto: String = "",

    // Información del reembolso
    val motivoReembolso: String = "",

    // Fotografía del comprobante
    val comprobanteUri: String = "",

    // Geolocalización del gasto
    val latitud: Double? = null,
    val longitud: Double? = null,

    // Errores de validación
    val errorTipoDocumento: String? = null,
    val errorNumeroDocumento: String? = null,
    val errorRutEmisor: String? = null,
    val errorNombreProveedor: String? = null,
    val errorFechaDocumento: String? = null,
    val errorMonto: String? = null,
    val errorMotivoReembolso: String? = null,

    // Error relacionado con el comprobante
    val errorComprobante: String? = null
)
