
package cl.teqmed.teqgastos.ui.fondorendir

import androidx.activity.compose.BackHandler
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val AzulTeqmed = Color(0xFF08638B)
private val VerdeTeqmed = Color(0xFF008267)
private val FondoTeqmed = Color(0xFFF5F8FA)
private val TextoPrincipal = Color(0xFF173E56)
private val TextoSecundario = Color(0xFF64748B)
private val BordeNormal = Color(0xFFDEE6EB)

@Composable
fun FondoRendirScreen(
    viewModel: FondoRendirViewModel,
    proyecto: String,
    onIngresarGasto: () -> Unit,
    onVolver: () -> Unit
) {
    BackHandler { onVolver() }
    val uiState = viewModel.uiState.collectAsState().value

    val formatoMoneda = NumberFormat.getNumberInstance(
        Locale("es", "CL")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoTeqmed)
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 16.dp,
                vertical = 24.dp
            )
    ) {

        TextButton(onClick = onVolver) {
            Text("← Volver", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AzulTeqmed)
        }
        Spacer(Modifier.height(12.dp))

        // Encabezado corporativo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fondo por rendir",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AzulTeqmed
            )

            Text(
                text = "TeqGastos",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = VerdeTeqmed
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Resumen del fondo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = proyecto,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipal
                    )

                    Text(
                        text = uiState.correlativo,
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Montos principales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Fondo asignado",
                            fontSize = 12.sp,
                            color = TextoSecundario
                        )

                        Text(
                            text = "$ ${formatoMoneda.format(uiState.fondoAsignado)}",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipal
                        )
                    }

                    Column {
                        Text(
                            text = "Total rendido",
                            fontSize = 12.sp,
                            color = TextoSecundario
                        )

                        Text(
                            text = "$ ${formatoMoneda.format(uiState.totalRendido)}",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Saldo destacado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFE9F5F3),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saldo disponible",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextoPrincipal
                    )

                    Text(
                        text = "$ ${formatoMoneda.format(uiState.saldoDisponible)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeTeqmed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Categorías autorizadas",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextoPrincipal
        )

        Spacer(modifier = Modifier.height(12.dp))

        CategoriaOpcion(
            texto = "Estacionamiento",
            tope = uiState.topeEstacionamiento,
            seleccionada =
                uiState.categoriaSeleccionada == "Estacionamiento",
            onClick = {
                viewModel.onCategoriaSeleccionada("Estacionamiento")
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        CategoriaOpcion(
            texto = "Peajes",
            tope = uiState.topePeajes,
            seleccionada =
                uiState.categoriaSeleccionada == "Peajes",
            onClick = {
                viewModel.onCategoriaSeleccionada("Peajes")
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        CategoriaOpcion(
            texto = "Alimentación",
            tope = uiState.topeAlimentacion,
            seleccionada =
                uiState.categoriaSeleccionada == "Alimentación",
            onClick = {
                viewModel.onCategoriaSeleccionada("Alimentación")
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        CategoriaOpcion(
            texto = "Materiales",
            tope = uiState.topeMateriales,
            seleccionada =
                uiState.categoriaSeleccionada == "Materiales",
            onClick = {
                viewModel.onCategoriaSeleccionada("Materiales")
            }
        )

        // Validación
        if (uiState.errorCategoria != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = uiState.errorCategoria,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                if (viewModel.validarCategoria()) {
                    onIngresarGasto()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulTeqmed
            )
        ) {
            Text(
                text = "Ingresar gasto",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
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

    val colorFondo = if (seleccionada) {
        Color(0xFFE9F5F3)
    } else {
        Color.White
    }

    val colorBorde = if (seleccionada) {
        VerdeTeqmed
    } else {
        BordeNormal
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                colorFondo,
                RoundedCornerShape(14.dp)
            )
            .border(
                width = if (seleccionada) 2.dp else 1.dp,
                color = colorBorde,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(
                horizontal = 14.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = texto,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoPrincipal
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Tope",
                fontSize = 11.sp,
                color = TextoSecundario
            )

            Text(
                text = "$ ${formatoMoneda.format(tope)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoPrincipal
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = if (seleccionada) "●" else "○",
            fontSize = 22.sp,
            color = if (seleccionada) {
                VerdeTeqmed
            } else {
                Color(0xFFA5B2BA)
            }
        )
    }
}
