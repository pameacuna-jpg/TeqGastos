package cl.teqmed.teqgastos.ui.fondorendir

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.teqmed.teqgastos.ui.theme.TeqmedAzulClaro
import cl.teqmed.teqgastos.ui.theme.TeqmedVerde
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FondoRendirScreen(
    viewModel: FondoRendirViewModel,
    proyecto: String,
    onIngresarGasto: () -> Unit
) {

    val uiState = viewModel.uiState.collectAsState().value

    val formatoMoneda = NumberFormat.getNumberInstance(
        Locale("es", "CL")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "TeqGastos",
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Fondo por Rendir",
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

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
                    text = uiState.correlativo
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Resumen del fondo",
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Column {

                        Text(text = "Fondo asignado")

                        Text(
                            text = "$ ${
                                formatoMoneda.format(
                                    uiState.fondoAsignado
                                )
                            }",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {

                        Text(text = "Total rendido")

                        Text(
                            text = "$ ${
                                formatoMoneda.format(
                                    uiState.totalRendido
                                )
                            }",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(text = "Saldo disponible")

                Text(
                    text = "$ ${
                        formatoMoneda.format(
                            uiState.saldoDisponible
                        )
                    }",
                    color = TeqmedVerde,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Categorías autorizadas",
            fontWeight = FontWeight.Bold
        )

        CategoriaOpcion(
            texto = "Estacionamiento",
            tope = uiState.topeEstacionamiento,
            seleccionada =
                uiState.categoriaSeleccionada ==
                        "Estacionamiento",
            onClick = {
                viewModel.onCategoriaSeleccionada(
                    "Estacionamiento"
                )
            }
        )

        CategoriaOpcion(
            texto = "Peajes",
            tope = uiState.topePeajes,
            seleccionada =
                uiState.categoriaSeleccionada ==
                        "Peajes",
            onClick = {
                viewModel.onCategoriaSeleccionada(
                    "Peajes"
                )
            }
        )

        CategoriaOpcion(
            texto = "Alimentación",
            tope = uiState.topeAlimentacion,
            seleccionada =
                uiState.categoriaSeleccionada ==
                        "Alimentación",
            onClick = {
                viewModel.onCategoriaSeleccionada(
                    "Alimentación"
                )
            }
        )

        CategoriaOpcion(
            texto = "Materiales",
            tope = uiState.topeMateriales,
            seleccionada =
                uiState.categoriaSeleccionada ==
                        "Materiales",
            onClick = {
                viewModel.onCategoriaSeleccionada(
                    "Materiales"
                )
            }
        )

        if (uiState.errorCategoria != null) {

            Text(
                text = uiState.errorCategoria
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                if (viewModel.validarCategoria()) {
                    onIngresarGasto()
                }
            }
        ) {

            Text(
                text = "Ingresar gasto"
            )
        }
    }
}

@Composable
fun CategoriaOpcion(
    texto: String,
    tope: Int,
    seleccionada: Boolean,
    onClick: () -> Unit
) {

    val formatoMoneda = NumberFormat.getNumberInstance(
        Locale("es", "CL")
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = seleccionada,
            onClick = onClick
        )

        Column {

            Text(
                text = texto,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tope autorizado: $ ${
                    formatoMoneda.format(tope)
                }"
            )
        }
    }
}