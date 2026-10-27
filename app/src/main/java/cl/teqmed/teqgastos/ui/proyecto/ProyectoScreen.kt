package cl.teqmed.teqgastos.ui.proyecto

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
            text = "Seleccionar proyecto",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Seleccione el proyecto al que corresponde la rendición de gastos.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // TARJETA DE PROYECTOS

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = TeqmedAzulClaro
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = "Proyectos disponibles",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TeqmedAzul
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                ProyectoOpcion(
                    nombre = "Proyecto 1001",
                    descripcion = "Seleccione para registrar gastos",
                    seleccionado =
                        uiState.proyectoSeleccionado == "Proyecto 1001",
                    onClick = {
                        viewModel.onProyectoSeleccionado(
                            "Proyecto 1001"
                        )
                    }
                )

                ProyectoOpcion(
                    nombre = "Proyecto 1002",
                    descripcion = "Seleccione para registrar gastos",
                    seleccionado =
                        uiState.proyectoSeleccionado == "Proyecto 1002",
                    onClick = {
                        viewModel.onProyectoSeleccionado(
                            "Proyecto 1002"
                        )
                    }
                )

                ProyectoOpcion(
                    nombre = "Proyecto 1003",
                    descripcion = "Seleccione para registrar gastos",
                    seleccionado =
                        uiState.proyectoSeleccionado == "Proyecto 1003",
                    onClick = {
                        viewModel.onProyectoSeleccionado(
                            "Proyecto 1003"
                        )
                    }
                )
            }
        }

        // ERROR

        if (uiState.errorProyecto != null) {

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = uiState.errorProyecto,
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
                if (viewModel.validarProyecto()) {
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
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProyectoOpcion(
    nombre: String,
    descripcion: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = seleccionado,
            onClick = onClick
        )

        Column(
            modifier = Modifier.padding(start = 6.dp)
        ) {

            Text(
                text = nombre,
                fontSize = 16.sp,
                fontWeight =
                    if (seleccionado) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    }
            )

            Text(
                text = descripcion,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}