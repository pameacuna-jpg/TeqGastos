
package cl.teqmed.teqgastos.ui.reembolso

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Azul = Color(0xFF08638B)
private val Verde = Color(0xFF008267)
private val Fondo = Color(0xFFF5F8FA)
private val Texto = Color(0xFF173E56)
private val Borde = Color(0xFFDEE6EB)

@Composable
fun ReembolsoScreen(
    proyecto: String,
    onContinuar: (String) -> Unit,
    onVolver: () -> Unit = {}
) {
    var categoriaSeleccionada by remember {
        mutableStateOf("")
    }

    var mostrarError by remember {
        mutableStateOf(false)
    }

    val categorias = listOf(
        "Estacionamiento",
        "Peajes",
        "Alimentación",
        "Materiales",
        "Alojamiento",
        "Combustible",
        "Otro"
    )

    BackHandler {
        onVolver()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {

        // CONTENIDO DESPLAZABLE
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp
                )
        ) {

            TextButton(
                onClick = onVolver
            ) {
                Text("← Volver", color = Azul)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Reembolso",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Azul
                )

                Text(
                    "TeqGastos",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Verde
                )
            }

            Spacer(Modifier.height(12.dp))

            // RESUMEN DEL PROYECTO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        proyecto,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Texto
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Gasto excepcional",
                        fontSize = 15.sp,
                        color = Verde
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "Seleccione la categoría",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Texto
            )

            Spacer(Modifier.height(10.dp))

            // CATEGORÍAS COMPACTAS
            categorias.forEach { categoria ->

                val seleccionada =
                    categoriaSeleccionada == categoria

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 7.dp)
                        .heightIn(min = 54.dp)
                        .background(
                            if (seleccionada) {
                                Color(0xFFE9F5F3)
                            } else {
                                Color.White
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (seleccionada) 2.dp else 1.dp,
                            color = if (seleccionada) Verde else Borde,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            categoriaSeleccionada = categoria
                            mostrarError = false
                        }
                        .padding(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    RadioButton(
                        selected = seleccionada,
                        onClick = {
                            categoriaSeleccionada = categoria
                            mostrarError = false
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Verde
                        )
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = categoria,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Texto
                    )
                }
            }

            if (mostrarError) {
                Text(
                    "Seleccione una categoría para continuar",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 15.sp
                )
            }
        }

        // BOTÓN FIJO INFERIOR
        Surface(
            color = Fondo,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 16.dp
                    )
            ) {
                Button(
                    onClick = {
                        if (categoriaSeleccionada.isBlank()) {
                            mostrarError = true
                        } else {
                            onContinuar(categoriaSeleccionada)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Azul
                    )
                ) {
                    Text(
                        "Continuar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
