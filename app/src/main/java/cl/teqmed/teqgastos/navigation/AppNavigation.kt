package cl.teqmed.teqgastos.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import android.widget.Toast
import java.io.OutputStreamWriter
import java.util.Calendar
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

import cl.teqmed.teqgastos.security.UsuarioInicializador
import cl.teqmed.teqgastos.ui.usuarios.GestionUsuariosScreen
import cl.teqmed.teqgastos.ui.usuarios.CrearUsuarioScreen
import cl.teqmed.teqgastos.ui.usuarios.EditarUsuarioScreen
import cl.teqmed.teqgastos.ui.usuarios.MiCuentaScreen
import cl.teqmed.teqgastos.data.local.UsuarioEntity
import cl.teqmed.teqgastos.security.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import cl.teqmed.teqgastos.data.local.GastoDatabaseProvider
import cl.teqmed.teqgastos.data.local.RendicionEntity
import cl.teqmed.teqgastos.data.local.toEntity
import cl.teqmed.teqgastos.data.local.toUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import cl.teqmed.teqgastos.ui.historial.HistorialRendicionesScreen
import cl.teqmed.teqgastos.ui.historial.RendicionHistorialUi
import cl.teqmed.teqgastos.ui.login.LoginScreen
import cl.teqmed.teqgastos.ui.login.LoginViewModel
import cl.teqmed.teqgastos.ui.proyecto.ProyectoScreen
import cl.teqmed.teqgastos.ui.proyecto.ProyectoViewModel
import cl.teqmed.teqgastos.ui.rendicion.RendicionScreen
import cl.teqmed.teqgastos.ui.rendicion.RendicionViewModel
import cl.teqmed.teqgastos.ui.categoriasfxr.CategoriasFxrScreen
import cl.teqmed.teqgastos.ui.categoriasfxr.guardarPorcentajesFxr
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
    val usuarioDao = remember(context) {
        GastoDatabaseProvider.obtenerDatabase(context).usuarioDao()
    }
    LaunchedEffect(usuarioDao) {
        UsuarioInicializador.crearAdministradorInicial(usuarioDao)
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
                usuarioDao = usuarioDao,
                onLoginCorrecto = {
                    val destino = when (loginViewModel.obtenerRol()) {
                        "SUPERVISOR" -> "supervisor"
                        "ADMINISTRADOR" -> "administrador"
                        else -> "inicio"
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
                onMiCuenta = { navController.navigate("mi_cuenta") },
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
                onMiCuenta = { navController.navigate("mi_cuenta") },
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

        composable("administrador") {
            MenuAdministrador(
                onUsuarios = { navController.navigate("gestion_usuarios") },
                onFinanzas = { navController.navigate("finanzas") },
                onCategoriasFxr = { navController.navigate("categorias_fxr") },
                onMiCuenta = { navController.navigate("mi_cuenta") },
                onSalir = { navController.popBackStack("login", inclusive = false) }
            )
        }

        composable("categorias_fxr") {
            CategoriasFxrScreen(
                categoriaDao = remember(context) {
                    GastoDatabaseProvider.obtenerDatabase(context).categoriaFxrDao()
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable("mi_cuenta") {
            val idSesion = loginViewModel.obtenerUsuarioId()
            if (idSesion != null) {
                MiCuentaScreen(
                    usuarioId = idSesion,
                    usuarioDao = usuarioDao,
                    onVolver = { volver() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }

        composable("gestion_usuarios") {
            val usuarios by usuarioDao.obtenerTodos()
                .collectAsState(initial = emptyList())

            GestionUsuariosScreen(
                usuarios = usuarios,
                onVolver = { volver() },
                onCrearUsuario = { navController.navigate("crear_usuario") },
                onCambiarEstado = { usuario ->
                    // La cuenta inicial de administrador no se puede desactivar.
                    if (usuario.nombreUsuario != "administrador") {
                        scope.launch {
                            val actual = usuarioDao.obtenerPorId(usuario.id)
                            if (actual != null && actual.nombreUsuario != "administrador") {
                                usuarioDao.actualizar(actual.copy(activo = !actual.activo))
                            }
                        }
                    }
                },
                onEditarUsuario = { usuario ->
                    navController.navigate("editar_usuario/${usuario.id}")
                }
            )
        }

        composable("editar_usuario/{usuarioId}") { entrada ->
            val usuarioId = entrada.arguments?.getString("usuarioId")?.toLongOrNull()
            var usuario by remember(usuarioId) { mutableStateOf<UsuarioEntity?>(null) }
            val database = remember(context) { GastoDatabaseProvider.obtenerDatabase(context) }
            val categoriaDao = remember(database) { database.categoriaFxrDao() }

            LaunchedEffect(usuarioId) {
                usuario = usuarioId?.let { usuarioDao.obtenerPorId(it) }
            }

            val usuarioActual = usuario
            if (usuarioActual != null) {
                EditarUsuarioScreen(
                    usuario = usuarioActual,
                    categoriaDao = categoriaDao,
                    onVolver = { volver() },
                    onGuardar = { usuarioEditado, porcentajes ->
                        scope.launch {
                            try {
                                val actual = usuarioDao.obtenerPorId(usuarioEditado.id)
                                if (actual != null) {
                                    // Primero validar y guardar la distribución FXR.
                                    // Si falla, no se modifican los datos del trabajador.
                                    if (porcentajes != null) {
                                        val guardado = guardarPorcentajesFxr(
                                            database = database,
                                            usuarioId = usuarioEditado.id,
                                            porcentajes = porcentajes
                                        )
                                        if (!guardado) {
                                            Toast.makeText(
                                                context,
                                                "No se guardó: revisa que los porcentajes sumen 100 %.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@launch
                                        }
                                    }
                                    usuarioDao.actualizar(
                                        actual.copy(
                                            nombre = usuarioEditado.nombre,
                                            apellido = usuarioEditado.apellido,
                                            correo = usuarioEditado.correo,
                                            telefono = usuarioEditado.telefono,
                                            rol = if (actual.nombreUsuario == "administrador")
                                                "ADMINISTRADOR" else usuarioEditado.rol,
                                            montoFxrAsignado = usuarioEditado.montoFxrAsignado
                                        )
                                    )
                                    volver()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "No se pudieron guardar los cambios: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                )
            } else {
                Text("Cargando usuario...")
            }
        }

        composable("crear_usuario") {
            var errorGuardar by remember { mutableStateOf<String?>(null) }
            var guardando by remember { mutableStateOf(false) }

            CrearUsuarioScreen(
                onVolver = { volver() },
                errorExterno = errorGuardar,
                guardando = guardando,
                onGuardar = { datos ->
                    if (!guardando) {
                        guardando = true
                        errorGuardar = null
                        scope.launch {
                            try {
                                val nombreUsuario = datos.nombreUsuario.trim().lowercase(Locale.ROOT)
                                if (usuarioDao.obtenerPorNombreUsuario(nombreUsuario) != null) {
                                    errorGuardar = "Ese nombre de usuario ya existe. Elija otro."
                                } else {
                                    val hash = withContext(Dispatchers.Default) {
                                        PasswordUtils.crearHash(datos.password)
                                    }
                                    usuarioDao.insertar(
                                        UsuarioEntity(
                                            nombre = datos.nombre,
                                            apellido = datos.apellido,
                                            nombreUsuario = nombreUsuario,
                                            passwordHash = hash,
                                            correo = datos.correo,
                                            telefono = datos.telefono,
                                            rol = datos.rol,
                                            activo = true,
                                            fechaCreacion = System.currentTimeMillis()
                                        )
                                    )
                                    navController.popBackStack()
                                }
                            } catch (e: android.database.sqlite.SQLiteConstraintException) {
                                errorGuardar = "Ese nombre de usuario ya existe. Elija otro."
                            } catch (e: Exception) {
                                errorGuardar = "No se pudo guardar el usuario. Intente nuevamente."
                            } finally {
                                guardando = false
                            }
                        }
                    }
                }
            )
        }

        composable("finanzas") {
            val rendiciones by rendicionDao.obtenerTodas()
                .collectAsState(initial = emptyList())
            PantallaAdministrador(
                rendiciones = rendiciones,
                gastos = gastos,
                onVolver = { volver() }
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
            val usuarioId = loginViewModel.obtenerUsuarioId()

            LaunchedEffect(usuarioId) {
                val usuarioActual = usuarioId?.let { id ->
                    usuarioDao.obtenerPorId(id)
                }
                fondoRendirViewModel.actualizarFondoAsignado(
                    usuarioActual?.montoFxrAsignado ?: 0L
                )
            }

            FondoRendirScreen(
                viewModel = fondoRendirViewModel,
                proyecto = proyectoState.proyectoSeleccionado,
                usuarioId = usuarioId,
                onVolver = { volver() },
                onIngresarGasto = {
                    tipoActual = "Fondo por Rendir"
                    categoriaActual = fondoState.categoriaSeleccionada

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
            val usuarioIdGasto = loginViewModel.obtenerUsuarioId()
            val databaseFxr = remember(context) {
                GastoDatabaseProvider.obtenerDatabase(context)
            }
            // -1 = cargando o sin autorización; 0 = reembolso (sin tope FXR).
            val topeFxr by produceState(
                initialValue = -1,
                usuarioIdGasto, categoriaActual, tipoActual, databaseFxr
            ) {
                value = -1
                if (tipoActual == "Reembolso") {
                    value = 0
                } else if (tipoActual == "Fondo por Rendir" && usuarioIdGasto != null) {
                    val usuario = usuarioDao.obtenerPorId(usuarioIdGasto)
                    val categoria = databaseFxr.categoriaFxrDao().obtenerCategorias()
                        .first().firstOrNull {
                            it.activa && it.nombre == categoriaActual
                        }
                    val porcentaje = categoria?.let {
                        databaseFxr.categoriaFxrDao()
                            .obtenerPorcentaje(usuarioIdGasto, it.id)
                    } ?: 0
                    if (usuario != null && usuario.activo &&
                        usuario.rol == "TRABAJADOR" && porcentaje > 0
                    ) {
                        value = ((usuario.montoFxrAsignado.coerceAtLeast(0L) *
                                porcentaje) / 100L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                    }
                }
            }

            if (topeFxr < 0) {
                Column(Modifier.fillMaxSize().padding(24.dp)) {
                    Text("Verificando categoría y autorización FXR…", color = Azul)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { volver() }) { Text("← Volver") }
                }
            } else {
                GastoScreen(
                    viewModel = gastoViewModel,
                    categoria = categoriaActual,
                    proyecto = proyectoState.proyectoSeleccionado,
                    esReembolso = tipoActual == "Reembolso",
                    esEdicion = idEditando != null,
                    topeCategoria = if (tipoActual == "Fondo por Rendir") topeFxr else null,

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
                            // Revalidar la autorización en Room justo antes de guardar.
                            if (tipoActual == "Fondo por Rendir") {
                                val idTrabajador = loginViewModel.obtenerUsuarioId()
                                val usuario = idTrabajador?.let { usuarioDao.obtenerPorId(it) }
                                val categoria = databaseFxr.categoriaFxrDao()
                                    .obtenerCategorias().first().firstOrNull {
                                        it.activa && it.nombre == categoriaActual
                                    }
                                val porcentaje = if (idTrabajador != null && categoria != null) {
                                    databaseFxr.categoriaFxrDao()
                                        .obtenerPorcentaje(idTrabajador, categoria.id) ?: 0
                                } else 0
                                if (usuario == null || !usuario.activo ||
                                    usuario.rol != "TRABAJADOR" || porcentaje <= 0
                                ) {
                                    Toast.makeText(context,
                                        "Categoría no autorizada. No se guardó el gasto.",
                                        Toast.LENGTH_LONG).show()
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
    onMiCuenta: () -> Unit,
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
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onMiCuenta, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Mi cuenta", color = Azul)
        }
        Spacer(Modifier.height(20.dp))
        TextButton(onClick = onSalir, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al inicio de sesión", color = Azul, fontSize = 15.sp)
        }
    }
}

// Panel académico del supervisor. Diseño corporativo TEQMED; decisiones guardadas en Room.
@Composable
private fun PantallaSupervisor(
    rendiciones: List<RendicionEntity>,
    gastos: List<GastoRegistrado>,
    onVolver: () -> Unit,
    onMiCuenta: () -> Unit,
    onResolver: (RendicionEntity, String, String) -> Unit
) {
    var seleccionada by remember { mutableStateOf<RendicionEntity?>(null) }
    var observacion by remember { mutableStateOf("") }
    var confirmarEstado by remember { mutableStateOf<String?>(null) }

    fun regresar() {
        if (seleccionada != null) {
            seleccionada = null
            observacion = ""
            confirmarEstado = null
        } else {
            onVolver()
        }
    }
    BackHandler { regresar() }

    val pendientes = rendiciones.filter { it.estado == "PENDIENTE_ENVIO" }
        .sortedByDescending { it.id }
    val revisadas = rendiciones.count { it.estado == "APROBADA" || it.estado == "RECHAZADA" }
    val formato = remember { NumberFormat.getNumberInstance(Locale("es", "CL")) }
    val textoSecundario = Color(0xFF526675)

    Column(
        modifier = Modifier.fillMaxSize().background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { regresar() }) { Text("← Volver", color = Azul) }
            TextButton(onClick = onMiCuenta) { Text("Mi cuenta", color = Azul) }
            Surface(color = Color(0xFFE0F1EC), shape = RoundedCornerShape(20.dp)) {
                Text("SUPERVISOR", color = Verde, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            if (seleccionada == null) "Panel del Supervisor"
            else "Rendición N.º ${seleccionada?.id}",
            fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (seleccionada == null) "Revisión y aprobación de rendiciones"
            else "Revisa los antecedentes antes de tomar una decisión",
            color = textoSecundario, fontSize = 14.sp
        )
        Spacer(Modifier.height(24.dp))

        if (seleccionada == null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("PENDIENTES", color = textoSecundario, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("${pendientes.size}", color = Azul, fontSize = 34.sp,
                            fontWeight = FontWeight.Bold)
                        Text("Por revisar", color = textoSecundario, fontSize = 12.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("REVISADAS", color = textoSecundario, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("$revisadas", color = Verde, fontSize = 34.sp,
                            fontWeight = FontWeight.Bold)
                        Text("Aprobadas o rechazadas", color = textoSecundario, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Bandeja de revisión", fontSize = 19.sp,
                fontWeight = FontWeight.Bold, color = Azul)
            Spacer(Modifier.height(12.dp))

            if (pendientes.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text("✓", fontSize = 40.sp, color = Verde, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        Text("¡Todo al día!", fontSize = 21.sp,
                            fontWeight = FontWeight.Bold, color = Azul)
                        Spacer(Modifier.height(8.dp))
                        Text("No hay rendiciones pendientes de revisión.",
                            color = textoSecundario, fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }

            pendientes.forEach { rendicion ->
                val total = gastos.filter { it.rendicionId == rendicion.id }
                    .sumOf { it.datos.monto.toDoubleOrNull() ?: 0.0 }
                Card(
                    onClick = { seleccionada = rendicion; observacion = "" },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Rendición N.º ${rendicion.id}", fontSize = 17.sp,
                            fontWeight = FontWeight.Bold, color = Azul)
                        Spacer(Modifier.height(6.dp))
                        Text("Proyecto ${rendicion.proyecto} · ${rendicion.tipoRendicion}",
                            color = textoSecundario, fontSize = 14.sp)
                        Spacer(Modifier.height(10.dp))
                        Text("Total: $ ${formato.format(total)}", fontSize = 19.sp,
                            fontWeight = FontWeight.Bold, color = Verde)
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFE7EDF1))
                        Spacer(Modifier.height(10.dp))
                        Text("Ver detalle y revisar →", color = Azul,
                            fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            val rendicion = seleccionada!!
            val gastosRendicion = gastos.filter { it.rendicionId == rendicion.id }
            val total = gastosRendicion.sumOf { it.datos.monto.toDoubleOrNull() ?: 0.0 }

            Card(
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Datos de la rendición", color = Azul, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text("Proyecto: ${rendicion.proyecto}")
                    Text("Tipo: ${rendicion.tipoRendicion}")
                    Spacer(Modifier.height(10.dp))
                    Text("Total: $ ${formato.format(total)}", color = Verde,
                        fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Gastos registrados", fontSize = 19.sp,
                fontWeight = FontWeight.Bold, color = Azul)
            Spacer(Modifier.height(10.dp))
            gastosRendicion.forEach { gasto ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(gasto.categoria, fontWeight = FontWeight.Bold,
                            color = Azul, fontSize = 16.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("${gasto.datos.tipoDocumento} ${gasto.datos.numeroDocumento}")
                        Text("Proveedor: ${gasto.datos.nombreProveedor}")
                        Text("RUT: ${gasto.datos.rutEmisor}")
                        if (gasto.datos.motivoReembolso.isNotBlank()) {
                            Text("Motivo: ${gasto.datos.motivoReembolso}")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("$ ${formato.format(gasto.datos.monto.toDoubleOrNull() ?: 0.0)}",
                            fontWeight = FontWeight.Bold, color = Verde, fontSize = 17.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = observacion,
                onValueChange = { observacion = it },
                label = { Text("Observación (obligatoria al rechazar)") },
                modifier = Modifier.fillMaxWidth(), minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { confirmarEstado = "APROBADA" },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Verde),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Aprobar rendición", fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { confirmarEstado = "RECHAZADA" },
                enabled = observacion.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Rechazar rendición", color = Rojo, fontWeight = FontWeight.SemiBold) }
        }
        Spacer(Modifier.height(24.dp))
        Text("TEQMED · TeqGastos", modifier = Modifier.fillMaxWidth(),
            color = textoSecundario, fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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

// Panel de Finanzas. Las rendiciones y sus gastos se consultan desde Room.
private fun campoCsv(valor: String): String = "\"" + valor.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\""

@Composable
private fun PantallaAdministrador(
    rendiciones: List<RendicionEntity>,
    gastos: List<GastoRegistrado>,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val hoy = remember { Calendar.getInstance() }
    var mes by remember { mutableIntStateOf(hoy.get(Calendar.MONTH)) }
    var anio by remember { mutableIntStateOf(hoy.get(Calendar.YEAR)) }
    var estado by remember { mutableStateOf<String?>(null) }
    var porTipo by remember { mutableStateOf(false) }
    var proyecto by remember { mutableStateOf<String?>(null) }
    var tipo by remember { mutableStateOf<String?>(null) }
    var rendicionId by remember { mutableStateOf<Long?>(null) }
    var gastoId by remember { mutableStateOf<Long?>(null) }
    var buscar by remember { mutableStateOf("") }
    var menuMes by remember { mutableStateOf(false) }
    var menuAnio by remember { mutableStateOf(false) }
    val formato = remember { NumberFormat.getNumberInstance(Locale("es", "CL")) }
    val secundario = Color(0xFF526675)
    val meses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val estados = listOf(Triple("APROBADA", "Aprobados", Verde), Triple("PENDIENTE_ENVIO", "Pendientes", Azul), Triple("RECHAZADA", "Rechazados", Rojo), Triple("BORRADOR", "Borradores", secundario))
    val tipos = listOf("Fondo por Rendir", "Reembolso", "TeqRuta")
    fun nombreTipo(t: String): String = when(t) { "Fondo por Rendir" -> "Fondos por Rendir"; "Reembolso" -> "Reembolsos"; else -> "TeqRuta" }
    fun nombreProyecto(c: String): String = if (c.startsWith("Proyecto ")) c else "Proyecto $c"
    fun total(r: RendicionEntity): Double = gastos.filter { it.rendicionId == r.id }.sumOf { it.datos.monto.toDoubleOrNull() ?: 0.0 }
    // Hasta registrar la fecha de aprobación, el período usa la fecha de cierre.
    fun delPeriodo(r: RendicionEntity): Boolean {
        val fecha = if ((r.fechaFinalizacion ?: 0L) > 0L) r.fechaFinalizacion!! else r.fechaCreacion
        if (fecha <= 0L) return false
        val cal = Calendar.getInstance().apply { timeInMillis = fecha }
        return cal.get(Calendar.MONTH) == mes && cal.get(Calendar.YEAR) == anio
    }
    val delEstado = rendiciones.filter { it.estado == estado && delPeriodo(it) }
    val proyectos = delEstado.map { it.proyecto }.distinct().sorted()
    val seleccionadas = delEstado.filter { (proyecto == null || it.proyecto == proyecto) && (tipo == null || it.tipoRendicion == tipo) }
    val rendicion = seleccionadas.firstOrNull { it.id == rendicionId }
    val gasto = gastos.firstOrNull { it.id == gastoId && it.rendicionId == rendicionId }
    fun volver() {
        when {
            gastoId != null -> gastoId = null
            rendicionId != null -> rendicionId = null
            tipo != null -> tipo = null
            proyecto != null -> proyecto = null
            porTipo -> porTipo = false
            estado != null -> estado = null
            else -> onVolver()
        }
    }
    BackHandler { volver() }

    // El archivo contiene una fila por gasto, no las fotografías.
    val paraExportar = when {
        rendicion != null -> listOf(rendicion)
        else -> seleccionadas
    }
    val exportador = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == android.app.Activity.RESULT_OK) {
            val uri = resultado.data?.data
            if (uri != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { salida ->
                        salida.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                        OutputStreamWriter(salida, Charsets.UTF_8).use { writer ->
                            val columnas = listOf("Rendicion", "Estado", "Proyecto", "Tipo rendicion", "Categoria", "Tipo documento", "Numero documento", "RUT proveedor", "Proveedor", "Fecha documento", "Monto CLP", "Motivo reembolso", "Observaciones")
                            writer.write(columnas.joinToString(";") { campoCsv(it) } + "\r\n")
                            paraExportar.forEach { r ->
                                gastos.filter { it.rendicionId == r.id }.forEach { g ->
                                    val d = g.datos
                                    val fila = listOf(r.id.toString(), r.estado, r.proyecto, r.tipoRendicion, g.categoria, d.tipoDocumento, d.numeroDocumento, d.rutEmisor, d.nombreProveedor, d.fechaDocumento, d.monto, d.motivoReembolso, r.observaciones)
                                    writer.write(fila.joinToString(";") { campoCsv(it) } + "\r\n")
                                }
                            }
                        }
                    } ?: error("No se pudo abrir el archivo")
                    Toast.makeText(context, "Informe CSV guardado", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "No se pudo guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    fun exportar() {
        val nombre = "TeqGastos_${anio}_${mes + 1}_${estado ?: "mes"}_${proyecto ?: "todos"}_${tipo ?: "todos"}.csv"
        exportador.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, nombre)
        })
    }
    @Composable
    fun Descargar(texto: String) {
        Button(onClick = { exportar() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Verde), shape = RoundedCornerShape(12.dp)) {
            Text(texto)
        }
    }
    @Composable
    fun TarjetaMovimiento(titulo: String, valor: String, onClick: () -> Unit) {
        Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(titulo, color = Azul, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(valor, color = Verde, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(12.dp))
                Text("›", color = Azul, fontSize = 22.sp)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Fondo).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { volver() }) { Text("← Volver", color = Azul) }
            Surface(color = Color(0xFFE0F1EC), shape = RoundedCornerShape(20.dp)) {
                Text("FINANZAS", color = Verde, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        if (estado == null) {
            Text("Movimientos del mes", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Azul)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(meses[mes], color = Azul, fontWeight = FontWeight.SemiBold)
                Text("$anio", color = Azul, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(20.dp))
            Text("Estado", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Azul)
            Spacer(Modifier.height(10.dp))
            estados.forEach { (clave, titulo, color) ->
                val cantidad = rendiciones.count { it.estado == clave && delPeriodo(it) }
                Card(onClick = { estado = clave; porTipo = false; proyecto = null; tipo = null; rendicionId = null; gastoId = null }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 17.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(titulo, color = color, fontWeight = FontWeight.SemiBold)
                        Text("$cantidad rendiciones  ›", color = secundario, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Filtrar por mes y año (opcional)", color = Azul, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) {
                    OutlinedButton(onClick = { menuMes = true }, modifier = Modifier.fillMaxWidth()) { Text(meses[mes]) }
                    DropdownMenu(expanded = menuMes, onDismissRequest = { menuMes = false }) {
                        meses.forEachIndexed { i, nombre -> DropdownMenuItem(text = { Text(nombre) }, onClick = { mes = i; menuMes = false }) }
                    }
                }
                Box(Modifier.weight(1f)) {
                    OutlinedButton(onClick = { menuAnio = true }, modifier = Modifier.fillMaxWidth()) { Text("$anio") }
                    DropdownMenu(expanded = menuAnio, onDismissRequest = { menuAnio = false }) {
                        (hoy.get(Calendar.YEAR) - 5..hoy.get(Calendar.YEAR) + 1).forEach { a -> DropdownMenuItem(text = { Text("$a") }, onClick = { anio = a; menuAnio = false }) }
                    }
                }
            }
        } else if (gasto != null) {
            Text("Detalle del gasto", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Azul)
            Spacer(Modifier.height(8.dp))
            Text("${nombreProyecto(rendicion!!.proyecto)} · Rendición N.º ${rendicion.id}", color = secundario)
            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text(gasto.categoria, color = Azul, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Documento: ${gasto.datos.tipoDocumento} ${gasto.datos.numeroDocumento}")
                    Text("Proveedor: ${gasto.datos.nombreProveedor}")
                    Text("RUT: ${gasto.datos.rutEmisor}")
                    Text("Fecha: ${gasto.datos.fechaDocumento}")
                    if (gasto.datos.motivoReembolso.isNotBlank()) Text("Motivo: ${gasto.datos.motivoReembolso}")
                    Spacer(Modifier.height(10.dp))
                    Text("$ ${formato.format(gasto.datos.monto.toDoubleOrNull() ?: 0.0)}", color = Verde, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Comprobante", color = Azul, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            if (gasto.datos.comprobanteUri.isBlank()) {
                Text("Este gasto no tiene fotografía adjunta.", color = secundario)
            } else {
                val foto = remember(gasto.datos.comprobanteUri) {
                    try {
                        context.contentResolver.openInputStream(Uri.parse(gasto.datos.comprobanteUri))?.use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
                    } catch (e: Exception) { null }
                }
                if (foto != null) {
                    Image(bitmap = foto, contentDescription = "Fotografía del comprobante", modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp), contentScale = ContentScale.Fit)
                } else Text("No fue posible abrir la fotografía guardada.", color = Rojo)
            }
            Spacer(Modifier.height(18.dp))
            Descargar("Descargar datos de la rendición (CSV)")
        } else if (rendicion != null) {
            Text("Rendición N.º ${rendicion.id}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Azul)
            Text("${nombreProyecto(rendicion.proyecto)} · ${rendicion.tipoRendicion}", color = secundario)
            Text("Estado: ${rendicion.estado.replace('_', ' ')}", color = Verde)
            if (rendicion.observaciones.isNotBlank()) Text("Observaciones: ${rendicion.observaciones}")
            Spacer(Modifier.height(18.dp))
            gastos.filter { it.rendicionId == rendicion.id }.forEach { g ->
                TarjetaMovimiento("${g.datos.tipoDocumento} ${g.datos.numeroDocumento}", "$ ${formato.format(g.datos.monto.toDoubleOrNull() ?: 0.0)}") { gastoId = g.id }
            }
            Spacer(Modifier.height(8.dp))
            Text("Total: $ ${formato.format(total(rendicion))}", color = Azul, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            Descargar("Descargar rendición (CSV)")
        } else if (tipo != null) {
            Text(nombreTipo(tipo!!), color = Azul, fontWeight = FontWeight.Bold, fontSize = 23.sp)
            Text("${meses[mes]} $anio · ${estados.first { it.first == estado }.second}", color = secundario)
            if (proyecto != null) Text(nombreProyecto(proyecto!!), color = secundario)
            Spacer(Modifier.height(18.dp))
            if (tipo == "TeqRuta") {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Text("Los kilómetros provienen de TeqRuta. Aún no existe conexión o importación, por lo que no hay datos para consultar ni descargar.", modifier = Modifier.padding(18.dp), color = secundario)
                }
            } else {
                if (seleccionadas.isEmpty()) Text("No hay movimientos de este tipo.", color = secundario)
                seleccionadas.sortedByDescending { it.id }.forEach { r ->
                    TarjetaMovimiento("${nombreProyecto(r.proyecto)} · Rendición N.º ${r.id}", "$ ${formato.format(total(r))}") { rendicionId = r.id }
                }
                if (seleccionadas.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Descargar("Descargar ${nombreTipo(tipo!!).lowercase()} (CSV)")
                }
            }
        } else if (proyecto != null) {
            Text(nombreProyecto(proyecto!!), color = Azul, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("${estados.first { it.first == estado }.second} · ${meses[mes]} $anio", color = secundario)
            Spacer(Modifier.height(16.dp))
            tipos.forEach { t ->
                Text(nombreTipo(t), color = Azul, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(6.dp))
                if (t == "TeqRuta") {
                    Text("Sin datos conectados", color = secundario)
                } else {
                    val rs = delEstado.filter { it.proyecto == proyecto && it.tipoRendicion == t }.sortedByDescending { it.id }
                    if (rs.isEmpty()) Text("Sin movimientos", color = secundario)
                    rs.forEach { r ->
                        TarjetaMovimiento("Rendición N.º ${r.id}", "$ ${formato.format(total(r))}") { rendicionId = r.id }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            Descargar("Descargar movimientos del proyecto (CSV)")
        } else if (porTipo) {
            Text("Movimientos por tipo", color = Azul, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("${estados.first { it.first == estado }.second} · ${meses[mes]} $anio", color = secundario)
            Spacer(Modifier.height(18.dp))
            tipos.forEach { t ->
                val rs = delEstado.filter { it.tipoRendicion == t }
                val valor = if (t == "TeqRuta") "Sin datos" else "$ ${formato.format(rs.sumOf { total(it) })}"
                TarjetaMovimiento(nombreTipo(t), valor) { tipo = t }
            }
            Spacer(Modifier.height(14.dp))
            if (delEstado.isNotEmpty()) Descargar("Descargar movimientos por tipo (CSV)")
        } else {
            Text(estados.first { it.first == estado }.second.uppercase(), color = Azul, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("${meses[mes]} $anio", color = secundario)
            Spacer(Modifier.height(16.dp))
            Text("Movimientos por proyecto", color = Azul, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (proyectos.isEmpty()) Text("No hay proyectos con movimientos en este período y estado.", color = secundario)
            proyectos.forEach { codigo ->
                val rs = delEstado.filter { it.proyecto == codigo }
                val fxr = rs.filter { it.tipoRendicion == "Fondo por Rendir" }.sumOf { total(it) }
                val reemb = rs.filter { it.tipoRendicion == "Reembolso" }.sumOf { total(it) }
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(nombreProyecto(codigo), color = Azul, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { proyecto = codigo }, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)) { Text("VER", color = Azul) }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("FxR", color = secundario); Text("$ ${formato.format(fxr)}", color = Azul)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Reembolsos", color = secundario); Text("$ ${formato.format(reemb)}", color = Azul)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TeqRuta", color = secundario); Text("Sin datos", color = secundario)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { porTipo = true }, modifier = Modifier.fillMaxWidth()) { Text("Movimientos por tipo", color = Azul) }
            Spacer(Modifier.height(12.dp))
            if (delEstado.isNotEmpty()) Descargar("Descargar movimientos del mes (CSV)")
            Spacer(Modifier.height(20.dp))
            Text("Buscar otros proyectos", color = Azul, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(value = buscar, onValueChange = { buscar = it }, label = { Text("Buscar por código") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            if (buscar.isNotBlank()) {
                proyectos.filter { it.contains(buscar.trim(), ignoreCase = true) }.forEach { codigo ->
                    TextButton(onClick = { proyecto = codigo; buscar = "" }) { Text(nombreProyecto(codigo), color = Azul) }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("TEQMED · TeqGastos", modifier = Modifier.fillMaxWidth(), color = secundario, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}


@Composable
private fun MenuAdministrador(
    onUsuarios: () -> Unit,
    onFinanzas: () -> Unit,
    onCategoriasFxr: () -> Unit,
    onMiCuenta: () -> Unit,
    onSalir: () -> Unit
) {
    BackHandler { onSalir() }
    Column(
        modifier = Modifier.fillMaxSize().background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = Azul,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TEQMED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 23.sp)
                Text("TeqGastos", color = Color.White, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(26.dp))
        Text("Panel del Administrador", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Azul)
        Spacer(Modifier.height(8.dp))
        Text("Seleccione la función que desea realizar", color = Color(0xFF526675))
        Spacer(Modifier.height(28.dp))
        Card(
            onClick = onUsuarios,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Administración de usuarios  ›", color = Azul, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("Crear cuentas, asignar roles y administrar permisos", color = Color(0xFF526675))
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(
            onClick = onCategoriasFxr,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Administración de categorías FXR  ›", color = Azul, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("Crear y administrar categorías del Fondo por Rendir", color = Color(0xFF526675))
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(
            onClick = onFinanzas,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Finanzas y movimientos  ›", color = Azul, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("Consultar rendiciones, filtrar y descargar informes CSV", color = Color(0xFF526675))
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(
            onClick = onMiCuenta,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Mi cuenta  ›", color = Azul, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("Consultar datos personales y cambiar contraseña", color = Color(0xFF526675))
            }
        }
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onSalir) { Text("Cerrar sesión", color = Azul) }
        Spacer(Modifier.height(12.dp))
        Text("TEQMED · TeqGastos", color = Color(0xFF526675), fontSize = 12.sp)
    }
}

