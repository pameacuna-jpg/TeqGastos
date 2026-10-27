package cl.teqmed.teqgastos.ui.rendicion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RendicionScreen(
    viewModel: RendicionViewModel
) {

    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(text = "TeqGastos")

        Text(text = "Tipo de rendición")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = uiState.tipoSeleccionado == "Fondo por Rendir",
                onClick = {
                    viewModel.onTipoSeleccionado("Fondo por Rendir")
                }
            )

            Text(text = "Fondo por Rendir")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = uiState.tipoSeleccionado == "Reembolso",
                onClick = {
                    viewModel.onTipoSeleccionado("Reembolso")
                }
            )

            Text(text = "Reembolso")
        }

        if (uiState.errorTipo != null) {
            Text(text = uiState.errorTipo)
        }

        Button(
            onClick = {
                viewModel.validarTipo()
            }
        ) {
            Text(text = "Continuar")
        }
    }
}
