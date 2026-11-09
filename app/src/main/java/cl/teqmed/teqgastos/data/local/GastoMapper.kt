package cl.teqmed.teqgastos.data.local

import cl.teqmed.teqgastos.ui.gastoregistro.GastoUiState

fun GastoUiState.toEntity(
    id: Long = 0,
    proyecto: String,
    tipoRendicion: String,
    categoria: String
): GastoEntity {

    return GastoEntity(
        id = id,
        proyecto = proyecto,
        tipoRendicion = tipoRendicion,
        categoria = categoria,
        tipoDocumento = tipoDocumento,
        numeroDocumento = numeroDocumento,
        rutEmisor = rutEmisor,
        nombreProveedor = nombreProveedor,
        fechaDocumento = fechaDocumento,
        monto = monto,
        motivoReembolso = motivoReembolso,
        comprobanteUri = comprobanteUri,
        latitud = latitud,
        longitud = longitud
    )
}

fun GastoEntity.toUiState(): GastoUiState {

    return GastoUiState(
        tipoDocumento = tipoDocumento,
        numeroDocumento = numeroDocumento,
        rutEmisor = rutEmisor,
        nombreProveedor = nombreProveedor,
        fechaDocumento = fechaDocumento,
        monto = monto,
        motivoReembolso = motivoReembolso,
        comprobanteUri = comprobanteUri,
        latitud = latitud,
        longitud = longitud
    )
}
