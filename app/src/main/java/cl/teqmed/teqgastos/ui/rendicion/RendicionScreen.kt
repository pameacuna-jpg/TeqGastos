package cl.teqmed.teqgastos.ui.rendicion

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.teqmed.teqgastos.ui.theme.TeqmedAzul
import cl.teqmed.teqgastos.ui.theme.TeqmedAzulClaro
import cl.teqmed.teqgastos.ui.theme.TeqmedVerde

@Composable
fun RendicionScreen(
    viewModel: RendicionViewModel,
    onContinuar: () -> Unit
) {

    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
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
            modifier = Modifier.height(28.dp)
        )

        // TÍTULO

        Text(
            text = "Tipo de rendición",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Seleccione el tipo de rendición que desea registrar.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // FONDO POR RENDIR

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = TeqmedAzulClaro
            )
        ) {

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                RadioButton(
                    selected =
                        uiState.tipoSeleccionado == "Fondo por Rendir",
                    onClick = {
                        viewModel.onTipoSeleccionado(
                            "Fondo por Rendir"
                        )
                    }
                )

                Column(
                    modifier = Modifier.padding(start = 6.dp)
                ) {

                    Text(
                        text = "Fondo por Rendir",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TeqmedAzul
                    )

                    Text(
                        text = "Registrar gastos asociados a un fondo previamente asignado.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // REEMBOLSO

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = TeqmedAzulClaro
            )
        ) {

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                RadioButton(
                    selected =
                        uiState.tipoSeleccionado == "Reembolso",
                    onClick = {
                        viewModel.onTipoSeleccionado(
                            "Reembolso"
                        )
                    }
                )

                Column(
                    modifier = Modifier.padding(start = 6.dp)
                ) {

                    Text(
                        text = "Reembolso",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TeqmedAzul
                    )

                    Text(
                        text = "Registrar gastos pagados directamente por el trabajador.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ERROR

        if (uiState.errorTipo != null) {

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = uiState.errorTipo,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // BOTÓN

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = {
                if (viewModel.validarTipo()) {
                    onContinuar()
                }
            }
        ) {

            Text(
                text = "Continuar",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "TEQMED · Soluciones que dan vida",
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            ),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}