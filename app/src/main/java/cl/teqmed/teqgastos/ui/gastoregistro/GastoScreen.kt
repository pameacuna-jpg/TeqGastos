
package cl.teqmed.teqgastos.ui.gastoregistro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val Azul = Color(0xFF08638B)
private val Verde = Color(0xFF008267)
private val Fondo = Color(0xFFF5F8FA)
private val Texto = Color(0xFF173E56)

private fun normalizarRut(rut: String): String {
    return rut.uppercase(Locale.ROOT)
        .replace(".", "")
        .replace("-", "")
        .replace(" ", "")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastoScreen(
    viewModel: GastoViewModel,
    categoria: String,
    proyecto: String,
    esReembolso: Boolean = false,
    buscarProveedor: (String) -> String? = { null },
    onVolver: () -> Unit = {},
    onConfirmar: (GastoUiState) -> Unit = {}
) {
    val estado = viewModel.uiState.collectAsState().value

    var mostrarCalendario by remember {
        mutableStateOf(false)
    }

    var mostrarConfirmacion by remember {
        mutableStateOf(false)
    }

    var advertirSalida by remember {
        mutableStateOf(false)
    }

    val calendario = rememberDatePickerState()

    val proveedorEncontrado =
        buscarProveedor(normalizarRut(estado.rutEmisor))

    LaunchedEffect(
        estado.rutEmisor,
        proveedorEncontrado
    ) {
        if (
            proveedorEncontrado != null &&
            estado.nombreProveedor != proveedorEncontrado
        ) {
            viewModel.onNombreProveedorChange(
                proveedorEncontrado
            )
        }
    }

    val hayDatos = estado.tipoDocumento.isNotBlank() ||
            estado.numeroDocumento.isNotBlank() ||
            estado.rutEmisor.isNotBlank() ||
            estado.nombreProveedor.isNotBlank() ||
            estado.fechaDocumento.isNotBlank() ||
            estado.monto.isNotBlank() ||
            estado.motivoReembolso.isNotBlank()

    fun volver() {
        if (hayDatos) {
            advertirSalida = true
        } else {
            onVolver()
        }
    }

    BackHandler {
        volver()
    }

    val formatoMoneda = NumberFormat.getNumberInstance(
        Locale("es", "CL")
    )

    val tope = when (categoria) {
        "Estacionamiento" -> 30000
        "Peajes" -> 50000
        "Alimentación" -> 50000
        "Materiales" -> 100000
        else -> 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TextButton(onClick = { volver() }) {
            Text("← Volver", color = Azul)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (esReembolso) {
                    "Registrar reembolso"
                } else {
                    "Registrar gasto"
                },
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Azul
            )

            Text(
                text = "TeqGastos",
                fontSize = 13.sp,
                color = Verde
            )
        }

        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text(
                    text = proyecto,
                    fontWeight = FontWeight.Bold,
                    color = Texto
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = categoria,
                    fontWeight = FontWeight.SemiBold,
                    color = Verde
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = if (esReembolso) {
                        "Gasto excepcional sujeto a revisión"
                    } else {
                        "Fondo por Rendir"
                    },
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                if (!esReembolso && tope > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Tope referencial: $ ${
                            formatoMoneda.format(tope)
                        }",
                        color = Verde,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text(
                    "Documento",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Texto
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Boleta", "Factura").forEach { tipo ->

                        val seleccionado =
                            estado.tipoDocumento == tipo

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel
                                        .onTipoDocumentoSeleccionado(tipo)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (seleccionado) {
                                    Color(0xFFE9F5F3)
                                } else {
                                    Fondo
                                }
                            )
                        ) {
                            Text(
                                text = tipo,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                color = if (seleccionado) Verde else Texto,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                estado.errorTipoDocumento?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = estado.numeroDocumento,
                    onValueChange =
                        viewModel::onNumeroDocumentoChange,
                    label = { Text("Número de documento") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError =
                        estado.errorNumeroDocumento != null,
                    supportingText = {
                        estado.errorNumeroDocumento?.let {
                            Text(it)
                        }
                    }
                )

                OutlinedTextField(
                    value = estado.rutEmisor,
                    onValueChange = { rut ->
                        viewModel.onRutEmisorChange(rut)

                        val encontrado =
                            buscarProveedor(normalizarRut(rut))

                        if (encontrado != null) {
                            viewModel.onNombreProveedorChange(
                                encontrado
                            )
                        } else if (proveedorEncontrado != null) {
                            // Evita conservar el proveedor anterior
                            // cuando se cambia a otro RUT
                            viewModel.onNombreProveedorChange("")
                        }
                    },
                    label = { Text("RUT del proveedor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorRutEmisor != null,
                    supportingText = {
                        estado.errorRutEmisor?.let {
                            Text(it)
                        }
                    }
                )

                if (proveedorEncontrado != null) {
                    Spacer(Modifier.height(4.dp))

                    Text(
                        "✓ Proveedor encontrado en registros anteriores",
                        color = Verde,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = estado.nombreProveedor,
                    onValueChange =
                        viewModel::onNombreProveedorChange,
                    label = {
                        Text("Razón social del proveedor")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = proveedorEncontrado != null,
                    isError =
                        estado.errorNombreProveedor != null,
                    supportingText = {
                        estado.errorNombreProveedor?.let {
                            Text(it)
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        mostrarCalendario = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (estado.fechaDocumento.isBlank()) {
                            "Seleccionar fecha del documento"
                        } else {
                            estado.fechaDocumento
                        }
                    )
                }

                estado.errorFechaDocumento?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = estado.monto,
                    onValueChange = viewModel::onMontoChange,
                    label = { Text("Monto del gasto ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    isError = estado.errorMonto != null,
                    supportingText = {
                        estado.errorMonto?.let {
                            Text(it)
                        }
                    }
                )
            }
        }

        if (esReembolso) {
            Spacer(Modifier.height(14.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        "Justificación del reembolso",
                        fontWeight = FontWeight.Bold,
                        color = Texto,
                        fontSize = 17.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Explique por qué fue necesario realizar este gasto.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = estado.motivoReembolso,
                        onValueChange =
                            viewModel::onMotivoReembolsoChange,
                        label = {
                            Text("Motivo obligatorio")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        isError =
                            estado.errorMotivoReembolso != null,
                        supportingText = {
                            estado.errorMotivoReembolso?.let {
                                Text(it)
                            }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Comprobante: Pendiente de implementar",
                    color = Texto
                )

                Spacer(Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))

                Text(
                    "Ubicación: Pendiente de implementar",
                    color = Texto
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (viewModel.validarGasto(esReembolso)) {
                    mostrarConfirmacion = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Azul
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Validar gasto",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(20.dp))
    }

    if (mostrarCalendario) {
        DatePickerDialog(
            onDismissRequest = {
                mostrarCalendario = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        calendario.selectedDateMillis?.let {
                            val formato = SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            )

                            formato.timeZone =
                                TimeZone.getTimeZone("UTC")

                            viewModel.onFechaDocumentoChange(
                                formato.format(Date(it))
                            )
                        }

                        mostrarCalendario = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarCalendario = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = calendario)
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = {
                mostrarConfirmacion = false
            },
            title = {
                Text("Confirmar gasto")
            },
            text = {
                Column {
                    Text("Categoría: $categoria")
                    Text("Proveedor: ${estado.nombreProveedor}")
                    Text("Monto: $ ${estado.monto}")

                    if (esReembolso) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Motivo: ${estado.motivoReembolso}"
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "¿Desea confirmar este registro?"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        onConfirmar(estado.copy())
                    }
                ) {
                    Text("Confirmar", color = Verde)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                    }
                ) {
                    Text("Editar")
                }
            }
        )
    }

    if (advertirSalida) {
        AlertDialog(
            onDismissRequest = {
                advertirSalida = false
            },
            title = {
                Text("Volver a la pantalla anterior")
            },
            text = {
                Text(
                    "Hay información ingresada. " +
                            "Si vuelve, los cambios aún no confirmados " +
                            "podrían perderse."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        advertirSalida = false
                        onVolver()
                    }
                ) {
                    Text("Volver")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        advertirSalida = false
                    }
                ) {
                    Text("Seguir editando")
                }
            }
        )
    }
}
