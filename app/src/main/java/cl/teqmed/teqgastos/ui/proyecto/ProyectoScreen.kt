package cl.teqmed.teqgastos.ui.proyecto

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
fun ProyectoScreen(
    viewModel: ProyectoViewModel,
    onContinuar: () -> Unit
) {

    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(text = "TeqGastos")

        Text(text = "Seleccionar proyecto")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = uiState.proyectoSeleccionado == "Proyecto 1001",
                onClick = {
                    viewModel.onProyectoSeleccionado("Proyecto 1001")
                }
            )
            Text(text = "Proyecto 1001")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = uiState.proyectoSeleccionado == "Proyecto 1002",
                onClick = {
                    viewModel.onProyectoSeleccionado("Proyecto 1002")
                }
            )
            Text(text = "Proyecto 1002")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = uiState.proyectoSeleccionado == "Proyecto 1003",
                onClick = {
                    viewModel.onProyectoSeleccionado("Proyecto 1003")
                }
            )
            Text(text = "Proyecto 1003")
        }

        if (uiState.errorProyecto != null) {
            Text(text = uiState.errorProyecto)
        }

        Button(
            onClick = {
                if (viewModel.validarProyecto()) {
                    onContinuar()
                }
            }
        ) {
            Text(text = "Continuar")
        }
    }
}