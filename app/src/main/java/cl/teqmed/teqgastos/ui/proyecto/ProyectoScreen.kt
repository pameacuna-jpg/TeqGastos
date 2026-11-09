
package cl.teqmed.teqgastos.ui.proyecto

import androidx.activity.compose.BackHandler
import androidx.compose.material3.TextButton
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AzulTeqmed = Color(0xFF08638B)
private val VerdeTeqmed = Color(0xFF008267)
private val FondoTeqmed = Color(0xFFF5F8FA)
private val BordeNormal = Color(0xFFDEE6EB)
private val TextoPrincipal = Color(0xFF173E56)

@Composable
fun ProyectoScreen(
    viewModel: ProyectoViewModel,
    onVolver: () -> Unit,
    onContinuar: () -> Unit
) {
    BackHandler { onVolver() }
    val uiState = viewModel.uiState.collectAsState().value

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoTeqmed)
    ) {
        val alturaPantalla = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = alturaPantalla - 48.dp),
                verticalArrangement = Arrangement.Center
            ) {

                TextButton(onClick = onVolver) {
                    Text("← Volver", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AzulTeqmed)
                }
                Spacer(Modifier.height(12.dp))

                // Encabezado compacto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Proyectos",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulTeqmed
                    )

                    Text(
                        text = "TeqGastos",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VerdeTeqmed
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opciones
                ProyectoOpcion(
                    nombre = "Proyecto 1001",
                    descripcion = "",
                    seleccionado =
                        uiState.proyectoSeleccionado == "Proyecto 1001",
                    onClick = {
                        viewModel.onProyectoSeleccionado("Proyecto 1001")
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProyectoOpcion(
                    nombre = "Proyecto 1002",
                    descripcion = "",
                    seleccionado =
                        uiState.proyectoSeleccionado == "Proyecto 1002",
                    onClick = {
                        viewModel.onProyectoSeleccionado("Proyecto 1002")
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProyectoOpcion(
                    nombre = "Proyecto 1003",
                    descripcion = "",
                    seleccionado =
                        uiState.proyectoSeleccionado == "Proyecto 1003",
                    onClick = {
                        viewModel.onProyectoSeleccionado("Proyecto 1003")
                    }
                )

                // Error de selección
                if (uiState.errorProyecto != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = uiState.errorProyecto,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (viewModel.validarProyecto()) {
                            onContinuar()
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
                        text = "Continuar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ProyectoOpcion(
    nombre: String,
    descripcion: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val borde = if (seleccionado) VerdeTeqmed else BordeNormal
    val fondo = if (seleccionado) {
        Color(0xFFE9F5F3)
    } else {
        Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(fondo, RoundedCornerShape(16.dp))
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = borde,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Icono de carpeta dibujado con Compose
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (seleccionado)
                        Color(0xFFD4ECE6)
                    else
                        Color(0xFFE8F2F6),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(24.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.10f, size.height * 0.30f)
                    lineTo(size.width * 0.42f, size.height * 0.30f)
                    lineTo(size.width * 0.53f, size.height * 0.43f)
                    lineTo(size.width * 0.90f, size.height * 0.43f)
                    lineTo(size.width * 0.90f, size.height * 0.83f)
                    lineTo(size.width * 0.10f, size.height * 0.83f)
                    close()
                }
                drawPath(
                    path = path,
                    color = if (seleccionado)
                        VerdeTeqmed
                    else
                        AzulTeqmed,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoPrincipal
            )

            if (seleccionado) {
                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Seleccionado",
                    fontSize = 14.sp,
                    color = VerdeTeqmed
                )
            }
        }

        // Indicador circular de selección
        Canvas(modifier = Modifier.size(24.dp)) {
            val color = if (seleccionado) {
                VerdeTeqmed
            } else {
                Color(0xFFA5B2BA)
            }

            drawCircle(
                color = color,
                radius = size.minDimension * 0.39f,
                style = if (seleccionado)
                    Stroke(width = 2.5.dp.toPx())
                else
                    Stroke(width = 1.5.dp.toPx())
            )

            if (seleccionado) {
                val marca = Path().apply {
                    moveTo(size.width * 0.29f, size.height * 0.51f)
                    lineTo(size.width * 0.44f, size.height * 0.65f)
                    lineTo(size.width * 0.72f, size.height * 0.34f)
                }

                drawPath(
                    path = marca,
                    color = VerdeTeqmed,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }
    }
}
