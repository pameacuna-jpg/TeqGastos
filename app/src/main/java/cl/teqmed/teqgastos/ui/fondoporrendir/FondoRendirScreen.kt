package cl.teqmed.teqgastos.ui.fondorendir

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
fun FondoRendirScreen(
    viewModel: FondoRendirViewModel,
    onIngresarGasto: () -> Unit
) {

    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(text = "TeqGastos")
        Text(text = "Fondo por Rendir")
        Text(text = "Seleccione categoría de gasto")

        CategoriaOpcion(
            texto = "Estacionamiento",
            seleccionada =
                uiState.categoriaSeleccionada == "Estacionamiento",
            onClick = {
                viewModel.onCategoriaSeleccionada("Estacionamiento")
            }
        )

        CategoriaOpcion(
            texto = "Peajes",
            seleccionada =
                uiState.categoriaSeleccionada == "Peajes",
            onClick = {
                viewModel.onCategoriaSeleccionada("Peajes")
            }
        )

        CategoriaOpcion(
            texto = "Alimentación",
            seleccionada =
                uiState.categoriaSeleccionada == "Alimentación",
            onClick = {
                viewModel.onCategoriaSeleccionada("Alimentación")
            }
        )

        CategoriaOpcion(
            texto = "Materiales",
            seleccionada =
                uiState.categoriaSeleccionada == "Materiales",
            onClick = {
                viewModel.onCategoriaSeleccionada("Materiales")
            }
        )

        if (uiState.errorCategoria != null) {
            Text(text = uiState.errorCategoria)
        }

        Button(
            onClick = {
                if (viewModel.validarCategoria()) {
                    onIngresarGasto()
                }
            }
        ) {
            Text(text = "Ingresar gasto")
        }
    }
}

@Composable
fun CategoriaOpcion(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = seleccionada,
            onClick = onClick
        )

        Text(text = texto)
    }
}