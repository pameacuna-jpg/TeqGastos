
package cl.teqmed.teqgastos.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import cl.teqmed.teqgastos.ui.login.LoginScreen
import cl.teqmed.teqgastos.ui.login.LoginViewModel
import cl.teqmed.teqgastos.ui.proyecto.ProyectoScreen
import cl.teqmed.teqgastos.ui.proyecto.ProyectoViewModel
import cl.teqmed.teqgastos.ui.rendicion.RendicionScreen
import cl.teqmed.teqgastos.ui.rendicion.RendicionViewModel
import cl.teqmed.teqgastos.ui.fondorendir.FondoRendirScreen
import cl.teqmed.teqgastos.ui.fondorendir.FondoRendirViewModel
import cl.teqmed.teqgastos.ui.reembolso.ReembolsoScreen
import cl.teqmed.teqgastos.ui.gastoregistro.GastoScreen
import cl.teqmed.teqgastos.ui.gastoregistro.GastoViewModel

import java.text.NumberFormat
import java.util.Locale

private val Azul = Color(0xFF08638B)
private val Verde = Color(0xFF008267)
private val Fondo = Color(0xFFF5F8FA)

private data class GastoRegistrado(
    val proyecto: String,
    val tipoRendicion: String,
    val categoria: String,
    val documento: String,
    val rutProveedor: String,
    val proveedor: String,
    val monto: Double,
    val motivo: String
)

private fun normalizarRut(rut: String): String {
    return rut.uppercase(Locale.ROOT)
        .replace(".", "")
        .replace("-", "")
        .replace(" ", "")
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val loginViewModel = remember { LoginViewModel() }
    val proyectoViewModel = remember { ProyectoViewModel() }
    val rendicionViewModel = remember { RendicionViewModel() }
    val fondoRendirViewModel = remember { FondoRendirViewModel() }
    val gastoViewModel = remember { GastoViewModel() }

    val proyectoState =
        proyectoViewModel.uiState.collectAsState().value

    val rendicionState =
        rendicionViewModel.uiState.collectAsState().value

    val fondoState =
        fondoRendirViewModel.uiState.collectAsState().value

    var tipoActual by remember { mutableStateOf("") }
    var categoriaActual by remember { mutableStateOf("") }

    val gastos = remember {
        mutableStateListOf<GastoRegistrado>()
    }

    val proveedores = remember {
        mutableStateMapOf<String, String>()
    }

    fun volver() {
        navController.popBackStack()
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginCorrecto = {
                    navController.navigate("proyecto")
                }
            )
        }

        composable("proyecto") {
            ProyectoScreen(
                viewModel = proyectoViewModel,
                onContinuar = {
                    navController.navigate("rendicion")
                }
            )
        }

        composable("rendicion") {
            RendicionScreen(
                viewModel = rendicionViewModel,
                onContinuar = {
                    tipoActual = rendicionState.tipoSeleccionado

                    when (tipoActual) {
                        "Fondo por Rendir" ->
                            navController.navigate("fondorendir")

                        "Reembolso" ->
                            navController.navigate("reembolso")
                    }
                }
            )
        }

        composable("fondorendir") {
            FondoRendirScreen(
                viewModel = fondoRendirViewModel,
                proyecto = proyectoState.proyectoSeleccionado,
                onIngresarGasto = {
                    tipoActual = "Fondo por Rendir"
                    categoriaActual =
                        fondoState.categoriaSeleccionada

                    gastoViewModel.limpiarFormulario()
                    navController.navigate("gasto")
                }
            )
        }

        composable("reembolso") {
            ReembolsoScreen(
                proyecto = proyectoState.proyectoSeleccionado,
                onVolver = { volver() },
                onContinuar = { categoria ->
                    tipoActual = "Reembolso"
                    categoriaActual = categoria

                    gastoViewModel.limpiarFormulario()
                    navController.navigate("gasto")
                }
            )
        }

        composable("gasto") {
            GastoScreen(
                viewModel = gastoViewModel,
                categoria = categoriaActual,
                proyecto = proyectoState.proyectoSeleccionado,
                esReembolso = tipoActual == "Reembolso",
                buscarProveedor = { rut ->
                    proveedores[normalizarRut(rut)]
                },
                onVolver = { volver() },
                onConfirmar = { estado ->

                    val rutNormalizado =
                        normalizarRut(estado.rutEmisor)

                    val proveedorExistente =
                        proveedores[rutNormalizado]

                    val nombreProveedor =
                        proveedorExistente
                            ?: estado.nombreProveedor.trim()

                    if (proveedorExistente == null) {
                        proveedores[rutNormalizado] =
                            nombreProveedor
                    }

                    gastos.add(
                        GastoRegistrado(
                            proyecto =
                                proyectoState.proyectoSeleccionado,
                            tipoRendicion = tipoActual,
                            categoria = categoriaActual,
                            documento =
                                "${estado.tipoDocumento} " +
                                        estado.numeroDocumento,
                            rutProveedor = rutNormalizado,
                            proveedor = nombreProveedor,
                            monto = estado.monto.toDoubleOrNull()
                                ?: 0.0,
                            motivo = estado.motivoReembolso
                        )
                    )

                    navController.navigate("resumen") {
                        popUpTo("gasto") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("resumen") {

            val gastosActuales = gastos.filter {
                it.proyecto ==
                        proyectoState.proyectoSeleccionado &&
                        it.tipoRendicion == tipoActual
            }

            ResumenGastosScreen(
                gastos = gastosActuales,
                proyecto = proyectoState.proyectoSeleccionado,
                tipo = tipoActual,

                onEliminar = { gasto ->
                    gastos.remove(gasto)
                },

                onAgregar = {
                    gastoViewModel.limpiarFormulario()

                    if (tipoActual == "Reembolso") {
                        navController.navigate("reembolso") {
                            popUpTo("reembolso") {
                                inclusive = true
                            }
                        }
                    } else {
                        navController.navigate("fondorendir") {
                            popUpTo("fondorendir") {
                                inclusive = true
                            }
                        }
                    }
                },

                onFinalizar = {
                    navController.navigate("finalizado")
                },

                onVolver = { volver() }
            )
        }

        composable("finalizado") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Fondo)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    "Rendición finalizada",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Verde
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Los gastos fueron registrados " +
                            "en esta sesión de demostración. " +
                            "Aún no se han enviado a un servidor."
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        tipoActual = ""
                        categoriaActual = ""
                        gastoViewModel.limpiarFormulario()

                        navController.navigate("proyecto") {
                            popUpTo("proyecto") {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nueva rendición")
                }
            }
        }
    }
}

@Composable
private fun ResumenGastosScreen(
    gastos: List<GastoRegistrado>,
    proyecto: String,
    tipo: String,
    onEliminar: (GastoRegistrado) -> Unit,
    onAgregar: () -> Unit,
    onFinalizar: () -> Unit,
    onVolver: () -> Unit
) {

    BackHandler {
        onVolver()
    }

    var gastoAEliminar by remember {
        mutableStateOf<GastoRegistrado?>(null)
    }

    val formato = NumberFormat.getNumberInstance(
        Locale("es", "CL")
    )

    val total = gastos.sumOf { it.monto }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TextButton(onClick = onVolver) {
            Text("← Volver", color = Azul)
        }

        Text(
            "Resumen de gastos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Azul
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "$proyecto · $tipo",
            color = Verde,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(16.dp))

        gastos.forEachIndexed { indice, gasto ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(Modifier.padding(16.dp)) {

                    Text(
                        "Gasto ${indice + 1}: ${gasto.categoria}",
                        fontWeight = FontWeight.Bold,
                        color = Azul
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(gasto.documento)

                    Text(
                        "RUT: ${gasto.rutProveedor}",
                        fontSize = 13.sp
                    )

                    Text(gasto.proveedor)

                    Text(
                        "$ ${formato.format(gasto.monto)}",
                        fontWeight = FontWeight.Bold,
                        color = Verde
                    )

                    if (gasto.motivo.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Motivo: ${gasto.motivo}",
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider()

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            gastoAEliminar = gasto
                        }
                    ) {
                        Text(
                            "Eliminar gasto",
                            color = Color(0xFFB3261E)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Total: $ ${formato.format(total)}",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Azul
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onAgregar,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Azul
            )
        ) {
            Text("Agregar otro gasto")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onFinalizar,
            enabled = gastos.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Finalizar rendición")
        }
    }

    gastoAEliminar?.let { gasto ->
        AlertDialog(
            onDismissRequest = {
                gastoAEliminar = null
            },
            title = {
                Text("Eliminar gasto")
            },
            text = {
                Text(
                    "¿Está seguro de eliminar el gasto " +
                            "de ${gasto.categoria} por $ " +
                            formato.format(gasto.monto) +
                            "? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminar(gasto)
                        gastoAEliminar = null
                    }
                ) {
                    Text(
                        "Eliminar",
                        color = Color(0xFFB3261E)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        gastoAEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
