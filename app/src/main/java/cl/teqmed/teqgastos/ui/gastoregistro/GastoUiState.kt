package cl.teqmed.teqgastos.ui.gastoregistro

data class GastoUiState(
    val fecha: String = "",
    val monto: String = "",
    val errorFecha: String? = null,
    val errorMonto: String? = null
)
