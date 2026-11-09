package cl.teqmed.teqgastos.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import cl.teqmed.teqgastos.data.local.GastoDatabaseProvider
import cl.teqmed.teqgastos.data.local.RendicionEntity
import cl.teqmed.teqgastos.data.local.toEntity
import cl.teqmed.teqgastos.data.local.toUiState
import kotlinx.coroutines.launch
import cl.teqmed.teqgastos.ui.historial.HistorialRendicionesScreen
import cl.teqmed.teqgastos.ui.historial.RendicionHistorialUi
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
import cl.teqmed.teqgastos.ui.gastoregistro.GastoUiState
import cl.teqmed.teqgastos.ui.gastoregistro.GastoViewModel

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Azul = Color(0xFF08638B)
private val Verde = Color(0xFF008267)
private val Fondo = Color(0xFFF5F8FA)
private val Rojo = Color(0xFFB3261E)

private data class GastoRegistrado(
    val id: Long,
    val rendicionId: Long?,
    val proyecto: String,
    val tipoRendicion: String,
    val categoria: String,
    val datos: GastoUiState
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember(context) {
        GastoDatabaseProvider.obtenerDatabase(context).gastoDao()
    }
    val rendicionDao = remember(context) {
        GastoDatabaseProvider.obtenerDatabase(context).rendicionDao()
    }
    val entidades by dao.obtenerTodos().collectAsState(initial = emptyList())
    val gastos = entidades.map { entidad ->
        GastoRegistrado(
            id = entidad.id,
            rendicionId = entidad.rendicionId,
            proyecto = entidad.proyecto,
            tipoRendicion = entidad.tipoRendicion,
            categoria = entidad.categoria,
            datos = entidad.toUiState()
        )
    }
    val proveedores = entidades.asReversed().associate { entidad ->
        normalizarRut(entidad.rutEmisor) to entidad.nombreProveedor
    }

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
    var rendicionActualId by remember { mutableStateOf<Long?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var categoriaActual by remember { mutableStateOf("") }

    // Si es null, estamos registrando un gasto nuevo.
    // Si tiene un ID, estamos editando ese gasto.
    var idEditando by remember {
        mutableStateOf<Long?>(null)
    }

    suspend fun obtenerOCrearBorrador(proyecto: String, tipo: String): Long {
        val existente = rendicionDao.obtenerBorrador(proyecto, tipo)
        return existente?.id ?: rendicionDao.insertar(
            RendicionEntity(proyecto = proyecto, tipoRendicion = tipo)
        )
    }

    fun volver() {
        navController.popBackStack()
    }

    fun irAlResumen() {
        navController.navigate("resumen") {
            popUpTo("resumen") {
                inclusive = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginCorrecto = {
                    val destino = if (loginViewModel.obtenerRol() == "SUPERVISOR") {
                        "supervisor"
                    } else {
                        "inicio"
                    }
                    navController.navigate(destino) {
                        popUpTo("login") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("inicio") {
            PantallaInicio(
                onNueva = { navController.navigate("proyecto") },
                onHistorial = { navController.navigate("historial") },
                onSalir = { navController.popBackStack() }
            )
        }

        composable("supervisor") {
            val rendiciones by rendicionDao.obtenerTodas()
                .collectAsState(initial = emptyList())

            PantallaSupervisor(
                rendiciones = rendiciones,
                gastos = gastos,
                onVolver = {
                    navController.popBackStack("login", inclusive = false)
                },
                onResolver = { rendicion, nuevoEstado, observacion ->
                    scope.launch {
                        val actual = rendicionDao.obtenerPorId(rendicion.id)
                        if (actual?.estado == "PENDIENTE_ENVIO") {
                            rendicionDao.actualizar(
                                actual.copy(
                                    estado = nuevoEstado,
                                    observaciones = observacion
                                )
                            )
                        }
                    }
                }
            )
        }

        composable("proyecto") {
            ProyectoScreen(
                viewModel = proyectoViewModel,
                onVolver = { volver() },
                onContinuar = {
                    rendicionActualId = null
                    mensajeError = null
                    navController.navigate("rendicion")
                }
            )
        }

        composable("rendicion") {
            RendicionScreen(
                viewModel = rendicionViewModel,
                onVolver = { volver() },
                onContinuar = {
                    val tipoElegido = rendicionState.tipoSeleccionado
                    val proyectoElegido = proyectoState.proyectoSeleccionado
                    if (tipoElegido == "Fondo por Rendir" || tipoElegido == "Reembolso") {
                        scope.launch {
                            tipoActual = tipoElegido
                            rendicionActualId = obtenerOCrearBorrador(proyectoElegido, tipoElegido)
                            mensajeError = null
                            navController.navigate(
                                if (tipoElegido == "Reembolso") "reembolso" else "fondorendir"
                            )
                        }
                    }
                },
                onVerGastosRegistrados = {
                    val tipoElegido = rendicionState.tipoSeleccionado
                    val proyectoElegido = proyectoState.proyectoSeleccionado
                    if (tipoElegido == "Fondo por Rendir" || tipoElegido == "Reembolso") {
                        scope.launch {
                            tipoActual = tipoElegido
                            rendicionActualId = obtenerOCrearBorrador(proyectoElegido, tipoElegido)
                            mensajeError = null
                            navController.navigate("resumen")
                        }
                    }
                }
            )
        }

        composable("fondorendir") {
            FondoRendirScreen(
                viewModel = fondoRendirViewModel,
                proyecto = proyectoState.proyectoSeleccionado,
                onVolver = { volver() },
                onIngresarGasto = {
                    tipoActual = "Fondo por Rendir"
                    categoriaActual =
                        fondoState.categoriaSeleccionada

                    idEditando = null
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

                    idEditando = null
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
                esEdicion = idEditando != null,

                buscarProveedor = { rut ->
                    proveedores[normalizarRut(rut)]
                },

                onVolver = {
                    if (idEditando != null) {
                        idEditando = null
                    }
                    volver()
                },

                onConfirmar = { estado ->

                    val rutNormalizado =
                        normalizarRut(estado.rutEmisor)

                    val nombreProveedor =
                        proveedores[rutNormalizado]
                            ?: estado.nombreProveedor.trim()

                    val datosConfirmados = estado.copy(
                        rutEmisor = rutNormalizado,
                        nombreProveedor = nombreProveedor,
                        errorTipoDocumento = null,
                        errorNumeroDocumento = null,
                        errorRutEmisor = null,
                        errorNombreProveedor = null,
                        errorFechaDocumento = null,
                        errorMonto = null,
                        errorMotivoReembolso = null
                    )

                    val idActual = idEditando
                    val rendicionId = rendicionActualId
                    scope.launch {
                        if (rendicionId == null) {
                            mensajeError = "No se encontró la rendición activa."
                            return@launch
                        }
                        val rendicion = rendicionDao.obtenerPorId(rendicionId)
                        if (rendicion?.estado != "BORRADOR") {
                            mensajeError = "Esta rendición está cerrada y no admite cambios."
                            irAlResumen()
                            return@launch
                        }
                        if (idActual != null) {
                            val anterior = dao.obtenerPorId(idActual)
                            if (anterior?.rendicionId != rendicionId) {
                                mensajeError = "El gasto no pertenece a esta rendición."
                                irAlResumen()
                                return@launch
                            }
                        }
                        val entidad = datosConfirmados.toEntity(
                            id = idActual ?: 0L,
                            proyecto = proyectoState.proyectoSeleccionado,
                            tipoRendicion = tipoActual,
                            categoria = categoriaActual,
                            rendicionId = rendicionId
                        )
                        if (idActual == null) dao.insertar(entidad)
                        else dao.actualizar(entidad)
                        idEditando = null
                        mensajeError = null
                        irAlResumen()
                    }
                }
            )
        }

        composable("resumen") {

            val gastosActuales = gastos.filter {
                rendicionActualId != null && it.rendicionId == rendicionActualId
            }
            val rendiciones by rendicionDao.obtenerTodas().collectAsState(initial = emptyList())
            val rendicionActual = rendiciones.firstOrNull { it.id == rendicionActualId }
            val editable = rendicionActual?.estado == "BORRADOR"

            ResumenGastosScreen(
                gastos = gastosActuales,
                proyecto = proyectoState.proyectoSeleccionado,
                tipo = tipoActual,
                editable = editable,
                mensajeError = mensajeError,

                onEditar = { gasto ->
                    if (editable && gasto.rendicionId == rendicionActualId) {
                        idEditando = gasto.id
                        categoriaActual = gasto.categoria
                        tipoActual = gasto.tipoRendicion

                        gastoViewModel.cargarGasto(gasto.datos)

                        navController.navigate("gasto")
                    }
                },

                onEliminar = { gasto ->
                    scope.launch {
                        val id = rendicionActualId
                        val estado = id?.let { rendicionDao.obtenerPorId(it)?.estado }
                        val actual = dao.obtenerPorId(gasto.id)
                        if (estado == "BORRADOR" && actual?.rendicionId == id) {
                            dao.eliminar(actual)
                        } else {
                            mensajeError = "No se puede eliminar un gasto de una rendición cerrada."
                        }
                    }
                },

                onAgregar = {
                    if (editable) {
                        idEditando = null
                        gastoViewModel.limpiarFormulario()

                        if (tipoActual == "Reembolso") {
                            navController.navigate("reembolso")
                        } else {
                            navController.navigate("fondorendir")
                        }
                    }
                },

                onFinalizar = {
                    val id = rendicionActualId
                    if (id != null && editable && gastosActuales.isNotEmpty()) {
                        scope.launch {
                            val resultado = rendicionDao.finalizarRendicion(
                                id, System.currentTimeMillis()
                            )
                            if (resultado == 1) {
                                mensajeError = null
                                navController.navigate("finalizado") {
                                    popUpTo("resumen") { inclusive = true }
                                }
                            } else {
                                mensajeError = "No fue posible finalizar la rendición."
                            }
                        }
                    }
                },

                onVolver = { volver() }
            )
        }

        composable("historial") {
            val rendicionesGuardadas by rendicionDao.obtenerTodas()
                .collectAsState(initial = emptyList())

            val formatoFecha = remember {
                SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
            }

            val historial = rendicionesGuardadas.map { rendicion ->
                val total = entidades
                    .filter { it.rendicionId == rendicion.id }
                    .sumOf { it.monto.toDoubleOrNull() ?: 0.0 }

                RendicionHistorialUi(
                    id = rendicion.id,
                    proyecto = rendicion.proyecto,
                    tipo = rendicion.tipoRendicion,
                    fecha = if (rendicion.fechaCreacion > 0L) {
                        formatoFecha.format(Date(rendicion.fechaCreacion))
                    } else {
                        "Fecha no disponible"
                    },
                    estado = rendicion.estado,
                    total = total.toLong()
                )
            }.sortedByDescending { it.id }

            HistorialRendicionesScreen(
                rendiciones = historial,
                onVolver = { volver() }
            )
        }

        composable("finalizado") {
            BackHandler(enabled = true) { /* Evitar volver a editar una rendición cerrada */ }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Fondo)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    "Rendición cerrada",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Verde
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "La rendición está pendiente de envío. " +
                            "Sus gastos ya no se pueden modificar. " +
                            "Todavía no se han enviado al servidor."
                )

                Spacer(Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { navController.navigate("historial") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver historial de rendiciones")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        tipoActual = ""
                        rendicionActualId = null
                        mensajeError = null
                        categoriaActual = ""
                        idEditando = null
                        gastoViewModel.limpiarFormulario()

                        navController.navigate("inicio") {
                            popUpTo("inicio") {
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
    editable: Boolean,
    mensajeError: String?,
    onEditar: (GastoRegistrado) -> Unit,
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

    val total = gastos.sumOf {
        it.datos.monto.toDoubleOrNull() ?: 0.0
    }

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

        if (!editable) {
            Text("Rendición cerrada: solo lectura", color = Verde)
            Spacer(Modifier.height(12.dp))
        }
        if (mensajeError != null) {
            Text(mensajeError, color = Rojo)
            Spacer(Modifier.height(12.dp))
        }

        gastos.forEachIndexed { indice, gasto ->

            val datos = gasto.datos
            val monto = datos.monto.toDoubleOrNull() ?: 0.0

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

                    Text(
                        "${datos.tipoDocumento} ${datos.numeroDocumento}"
                    )

                    Text(
                        "RUT: ${datos.rutEmisor}",
                        fontSize = 13.sp
                    )

                    Text(datos.nombreProveedor)

                    Text(
                        "Fecha: ${datos.fechaDocumento}",
                        fontSize = 13.sp
                    )

                    Text(
                        "$ ${formato.format(monto)}",
                        fontWeight = FontWeight.Bold,
                        color = Verde
                    )

                    if (datos.motivoReembolso.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Motivo: ${datos.motivoReembolso}",
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    if (editable) Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        TextButton(
                            onClick = {
                                onEditar(gasto)
                            }
                        ) {
                            Text(
                                "✎ Editar gasto",
                                color = Azul
                            )
                        }

                        TextButton(
                            onClick = {
                                gastoAEliminar = gasto
                            }
                        ) {
                            Text(
                                "Eliminar gasto",
                                color = Rojo
                            )
                        }
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
            enabled = editable,
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
            enabled = editable && gastos.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Finalizar rendición")
        }
    }

    gastoAEliminar?.let { gasto ->

        val monto =
            gasto.datos.monto.toDoubleOrNull() ?: 0.0

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
                            formato.format(monto) +
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
                        color = Rojo
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

@Composable
private fun PantallaInicio(
    onNueva: () -> Unit,
    onHistorial: () -> Unit,
    onSalir: () -> Unit
) {
    BackHandler { onSalir() }
    Column(
        modifier = Modifier.fillMaxSize().background(Fondo)
            .padding(horizontal = 24.dp, vertical = 42.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("TeqGastos", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Azul)
        Spacer(Modifier.height(8.dp))
        Text("Gestión de rendiciones de gastos", fontSize = 16.sp, color = Color(0xFF334155))
        Spacer(Modifier.height(36.dp))
        Button(onClick = onNueva, modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Azul)) {
            Text("Nueva rendición", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onHistorial, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Mis rendiciones", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Verde)
        }
        Spacer(Modifier.height(20.dp))
        TextButton(onClick = onSalir, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al inicio de sesión", color = Azul, fontSize = 15.sp)
        }
    }
}

// Panel académico del supervisor. Los cambios se guardan en Room.
@Composable
private fun PantallaSupervisor(
    rendiciones: List<RendicionEntity>,
    gastos: List<GastoRegistrado>,
    onVolver: () -> Unit,
    onResolver: (RendicionEntity, String, String) -> Unit
) {
    BackHandler { onVolver() }

    var seleccionada by remember { mutableStateOf<RendicionEntity?>(null) }
    var observacion by remember { mutableStateOf("") }
    var confirmarEstado by remember { mutableStateOf<String?>(null) }

    val pendientes = rendiciones.filter { it.estado == "PENDIENTE_ENVIO" }
        .sortedByDescending { it.id }
    val formato = remember { NumberFormat.getNumberInstance(Locale("es", "CL")) }

    Column(
        modifier = Modifier.fillMaxSize().background(Fondo)
            .verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        TextButton(onClick = {
            if (seleccionada != null) {
                seleccionada = null
                observacion = ""
            } else {
                onVolver()
            }
        }) { Text("← Volver", color = Azul) }

        Text(
            if (seleccionada == null) "Panel del Supervisor"
            else "Rendición N.º ${seleccionada?.id}",
            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Azul
        )
        Spacer(Modifier.height(8.dp))

        if (seleccionada == null) {
            Text("Rendiciones pendientes: ${pendientes.size}", color = Verde)
            Spacer(Modifier.height(20.dp))
            if (pendientes.isEmpty()) {
                Text("No hay rendiciones pendientes de revisión.")
            }
            pendientes.forEach { rendicion ->
                val total = gastos.filter { it.rendicionId == rendicion.id }
                    .sumOf { it.datos.monto.toDoubleOrNull() ?: 0.0 }
                Card(
                    onClick = {
                        seleccionada = rendicion
                        observacion = ""
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Rendición N.º ${rendicion.id}",
                            fontWeight = FontWeight.Bold, color = Azul)
                        Text("${rendicion.proyecto} · ${rendicion.tipoRendicion}")
                        Text("Total: $ ${formato.format(total)}",
                            fontWeight = FontWeight.Bold, color = Verde)
                        Spacer(Modifier.height(8.dp))
                        Text("Tocar para revisar", color = Azul)
                    }
                }
            }
        } else {
            val rendicion = seleccionada!!
            val gastosRendicion = gastos.filter { it.rendicionId == rendicion.id }
            Text("Proyecto: ${rendicion.proyecto}")
            Text("Tipo: ${rendicion.tipoRendicion}")
            Spacer(Modifier.height(16.dp))
            Text("Gastos registrados", fontWeight = FontWeight.Bold, color = Azul)
            Spacer(Modifier.height(8.dp))
            gastosRendicion.forEach { gasto ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(gasto.categoria, fontWeight = FontWeight.Bold)
                        Text("${gasto.datos.tipoDocumento} ${gasto.datos.numeroDocumento}")
                        Text("Proveedor: ${gasto.datos.nombreProveedor}")
                        Text("RUT: ${gasto.datos.rutEmisor}")
                        Text("Monto: $ ${formato.format(gasto.datos.monto.toDoubleOrNull() ?: 0.0)}")
                        if (gasto.datos.motivoReembolso.isNotBlank()) {
                            Text("Motivo: ${gasto.datos.motivoReembolso}")
                        }
                    }
                }
            }
            val total = gastosRendicion.sumOf {
                it.datos.monto.toDoubleOrNull() ?: 0.0
            }
            Text("Total: $ ${formato.format(total)}",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Azul)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = observacion,
                onValueChange = { observacion = it },
                label = { Text("Observación (obligatoria al rechazar)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { confirmarEstado = "APROBADA" },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Verde)
            ) { Text("Aprobar rendición") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { confirmarEstado = "RECHAZADA" },
                enabled = observacion.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Rechazar rendición", color = Rojo) }
        }
    }

    confirmarEstado?.let { estado ->
        AlertDialog(
            onDismissRequest = { confirmarEstado = null },
            title = { Text(if (estado == "APROBADA") "Aprobar rendición" else "Rechazar rendición") },
            text = { Text("¿Confirmas esta decisión? Se guardará en el historial.") },
            confirmButton = {
                TextButton(onClick = {
                    seleccionada?.let { onResolver(it, estado, observacion.trim()) }
                    confirmarEstado = null
                    seleccionada = null
                    observacion = ""
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEstado = null }) { Text("Cancelar") }
            }
        )
    }
}

