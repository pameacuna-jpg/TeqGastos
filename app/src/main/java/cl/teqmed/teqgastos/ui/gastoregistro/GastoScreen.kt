package cl.teqmed.teqgastos.ui.gastoregistro

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.teqmed.teqgastos.ui.theme.TeqmedAzul
import cl.teqmed.teqgastos.ui.theme.TeqmedAzulClaro
import cl.teqmed.teqgastos.ui.theme.TeqmedVerde
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GastoScreen(
    viewModel: GastoViewModel,
    categoria: String,
    proyecto: String
) {

    val uiState = viewModel.uiState.collectAsState().value

    var mostrarCalendario by remember {
        mutableStateOf(false)
    }

    val datePickerState = rememberDatePickerState()

    val formatoMoneda = NumberFormat.getNumberInstance(
        Locale("es", "CL")
    )

    // Tope según la categoría seleccionada
    val topeAutorizado = when (categoria) {

        "Estacionamiento" -> 30000

        "Peajes" -> 50000

        "Alimentación" -> 50000

        "Materiales" -> 100000

        else -> 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        // ENCABEZADO

        Text(
            text = "TEQMED",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TeqmedAzul
        )

        Text(
            text = "TeqGastos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TeqmedVerde
        )

        Text(
            text = "Rendición de gastos en terreno",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Registrar gasto",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        // INFORMACIÓN DEL FXR

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = TeqmedAzulClaro
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = proyecto,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "FXR N.º 0001"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Categoría",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = categoria,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TeqmedAzul
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Tope autorizado: $ ${
                        formatoMoneda.format(topeAutorizado)
                    }",
                    fontWeight = FontWeight.Bold,
                    color = TeqmedVerde
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // TIPO DE DOCUMENTO

        Text(
            text = "Tipo de documento",
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            RadioButton(
                selected = uiState.tipoDocumento == "Boleta",
                onClick = {
                    viewModel.onTipoDocumentoSeleccionado(
                        "Boleta"
                    )
                }
            )

            Text(text = "Boleta")

            RadioButton(
                selected = uiState.tipoDocumento == "Factura",
                onClick = {
                    viewModel.onTipoDocumentoSeleccionado(
                        "Factura"
                    )
                }
            )

            Text(text = "Factura")
        }

        if (uiState.errorTipoDocumento != null) {

            Text(
                text = uiState.errorTipoDocumento,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // FECHA DEL DOCUMENTO

        Text(
            text = "Fecha del documento",
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Button(
            onClick = {
                mostrarCalendario = true
            }
        ) {

            Text(
                text = if (uiState.fechaDocumento.isBlank()) {
                    "Seleccionar fecha"
                } else {
                    uiState.fechaDocumento
                }
            )
        }

        if (uiState.errorFechaDocumento != null) {

            Text(
                text = uiState.errorFechaDocumento,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        // MONTO

        OutlinedTextField(
            value = uiState.monto,
            onValueChange = {
                viewModel.onMontoChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Monto")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            isError = uiState.errorMonto != null,
            supportingText = {

                if (uiState.errorMonto != null) {

                    Text(
                        text = uiState.errorMonto
                    )
                }
            }
        )

        // COMPROBANTE

        Text(
            text = "Comprobante",
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Fotografía pendiente",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // UBICACIÓN

        Text(
            text = "Ubicación",
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Ubicación pendiente",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // GUARDAR

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = {

                viewModel.validarGasto()
            }
        ) {

            Text(
                text = "Guardar gasto",
                fontWeight = FontWeight.Bold
            )
        }
    }

    // CALENDARIO

    if (mostrarCalendario) {

        DatePickerDialog(
            onDismissRequest = {
                mostrarCalendario = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        val fechaSeleccionada =
                            datePickerState.selectedDateMillis

                        if (fechaSeleccionada != null) {

                            val formato = SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            )

                            val fechaFormateada =
                                formato.format(
                                    Date(fechaSeleccionada)
                                )

                            viewModel.onFechaDocumentoChange(
                                fechaFormateada
                            )
                        }

                        mostrarCalendario = false
                    }
                ) {

                    Text(text = "Aceptar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarCalendario = false
                    }
                ) {

                    Text(text = "Cancelar")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }
}