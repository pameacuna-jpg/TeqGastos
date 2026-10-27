package cl.teqmed.teqgastos.ui.gastoregistro

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GastoScreen(
    viewModel: GastoViewModel
) {

    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(text = "TeqGastos")

        Text(text = "Registrar gasto")

        OutlinedTextField(
            value = uiState.fecha,
            onValueChange = {
                viewModel.onFechaChange(it)
            },
            label = {
                Text(text = "Fecha")
            },
            isError = uiState.errorFecha != null,
            supportingText = {
                if (uiState.errorFecha != null) {
                    Text(text = uiState.errorFecha)
                }
            }
        )

        OutlinedTextField(
            value = uiState.monto,
            onValueChange = {
                viewModel.onMontoChange(it)
            },
            label = {
                Text(text = "Monto")
            },
            isError = uiState.errorMonto != null,
            supportingText = {
                if (uiState.errorMonto != null) {
                    Text(text = uiState.errorMonto)
                }
            }
        )

        Text(text = "Comprobante: pendiente")

        Text(text = "Ubicación: pendiente")

        Button(
            onClick = {
                viewModel.validarGasto()
            }
        ) {
            Text(text = "Guardar gasto")
        }
    }
}
