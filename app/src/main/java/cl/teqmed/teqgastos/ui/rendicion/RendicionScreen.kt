package cl.teqmed.teqgastos.ui.rendicion

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
import androidx.compose.material3.OutlinedButton
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
fun RendicionScreen(
    viewModel: RendicionViewModel,
    onContinuar: () -> Unit,
    onVerGastosRegistrados: () -> Unit
) {
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
                .padding(
                    horizontal = 20.dp,
                    vertical = 24.dp
                ),
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = alturaPantalla - 48.dp),
                verticalArrangement = Arrangement.Center
            ) {

                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tipo de rendición",
                        fontSize = 25.sp,
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

                Spacer(modifier = Modifier.height(20.dp))

                // Opción Fondo por Rendir
                TipoRendicionOpcion(
                    titulo = "Fondo por Rendir",
                    descripcion = "Dinero asignado",
                    esFondo = true,
                    seleccionado =
                        uiState.tipoSeleccionado == "Fondo por Rendir",
                    onClick = {
                        viewModel.onTipoSeleccionado(
                            "Fondo por Rendir"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Opción Reembolso
                TipoRendicionOpcion(
                    titulo = "Reembolso",
                    descripcion = "Gasto del trabajador",
                    esFondo = false,
                    seleccionado =
                        uiState.tipoSeleccionado == "Reembolso",
                    onClick = {
                        viewModel.onTipoSeleccionado(
                            "Reembolso"
                        )
                    }
                )

                // Mensaje de validación
                if (uiState.errorTipo != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = uiState.errorTipo,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (viewModel.validarTipo()) {
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

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        if (viewModel.validarTipo()) {
                            onVerGastosRegistrados()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Ver gastos registrados",
                        color = AzulTeqmed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TipoRendicionOpcion(
    titulo: String,
    descripcion: String,
    esFondo: Boolean,
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
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Icono corporativo sin bibliotecas adicionales
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
                val colorIcono = if (seleccionado) {
                    VerdeTeqmed
                } else {
                    AzulTeqmed
                }

                if (esFondo) {
                    // Billetera
                    drawRoundRect(
                        color = colorIcono,
                        topLeft = Offset(
                            size.width * 0.12f,
                            size.height * 0.28f
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            size.width * 0.76f,
                            size.height * 0.55f
                        ),
                        cornerRadius =
                            androidx.compose.ui.geometry.CornerRadius(
                                4.dp.toPx()
                            ),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    drawLine(
                        color = colorIcono,
                        start = Offset(
                            size.width * 0.55f,
                            size.height * 0.53f
                        ),
                        end = Offset(
                            size.width * 0.88f,
                            size.height * 0.53f
                        ),
                        strokeWidth = 2.dp.toPx()
                    )
                } else {
                    // Comprobante
                    val recibo = Path().apply {
                        moveTo(size.width * 0.24f, size.height * 0.12f)
                        lineTo(size.width * 0.76f, size.height * 0.12f)
                        lineTo(size.width * 0.76f, size.height * 0.88f)
                        lineTo(size.width * 0.63f, size.height * 0.80f)
                        lineTo(size.width * 0.50f, size.height * 0.88f)
                        lineTo(size.width * 0.37f, size.height * 0.80f)
                        lineTo(size.width * 0.24f, size.height * 0.88f)
                        close()
                    }

                    drawPath(
                        path = recibo,
                        color = colorIcono,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    drawLine(
                        color = colorIcono,
                        start = Offset(
                            size.width * 0.35f,
                            size.height * 0.40f
                        ),
                        end = Offset(
                            size.width * 0.65f,
                            size.height * 0.40f
                        ),
                        strokeWidth = 2.dp.toPx()
                    )

                    drawLine(
                        color = colorIcono,
                        start = Offset(
                            size.width * 0.35f,
                            size.height * 0.57f
                        ),
                        end = Offset(
                            size.width * 0.60f,
                            size.height * 0.57f
                        ),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoPrincipal
            )

            Text(
                text = descripcion,
                fontSize = 12.sp,
                color = Color.Gray
            )

            if (seleccionado) {
                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Seleccionado",
                    fontSize = 12.sp,
                    color = VerdeTeqmed
                )
            }
        }

        // Indicador de selección
        Canvas(modifier = Modifier.size(24.dp)) {
            val color = if (seleccionado) {
                VerdeTeqmed
            } else {
                Color(0xFFA5B2BA)
            }

            drawCircle(
                color = color,
                radius = size.minDimension * 0.39f,
                style = Stroke(
                    width = if (seleccionado)
                        2.5.dp.toPx()
                    else
                        1.5.dp.toPx()
                )
            )

            if (seleccionado) {
                val marca = Path().apply {
                    moveTo(
                        size.width * 0.29f,
                        size.height * 0.51f
                    )
                    lineTo(
                        size.width * 0.44f,
                        size.height * 0.65f
                    )
                    lineTo(
                        size.width * 0.72f,
                        size.height * 0.34f
                    )
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
