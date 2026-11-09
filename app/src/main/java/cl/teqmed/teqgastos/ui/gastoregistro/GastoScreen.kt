package cl.teqmed.teqgastos.ui.gastoregistro

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.core.os.CancellationSignal
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.location.LocationManagerCompat
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val Azul = Color(0xFF08638B)
private val Verde = Color(0xFF008267)
private val Fondo = Color(0xFFF5F8FA)
private val Texto = Color(0xFF173E56)

private fun normalizarRut(rut: String): String {
    return rut.uppercase(Locale.ROOT)
        .replace(".", "")
        .replace("-", "")
        .replace(" ", "")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastoScreen(
    viewModel: GastoViewModel,
    categoria: String,
    proyecto: String,
    esReembolso: Boolean = false,
    esEdicion: Boolean = false,
    buscarProveedor: (String) -> String? = { null },
    onVolver: () -> Unit = {},
    onConfirmar: (GastoUiState) -> Unit = {}
) {
    val context = LocalContext.current
    val estado = viewModel.uiState.collectAsState().value

    var mostrarCalendario by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var advertirSalida by remember { mutableStateOf(false) }
    var uriFotoPendiente by remember { mutableStateOf<Uri?>(null) }
    var errorFoto by remember { mutableStateOf<String?>(null) }
    var mensajeUbicacion by remember { mutableStateOf<String?>(null) }
    var buscandoUbicacion by remember { mutableStateOf(false) }
    var cancelacionUbicacion by remember { mutableStateOf<CancellationSignal?>(null) }

    DisposableEffect(Unit) {
        onDispose { cancelacionUbicacion?.cancel() }
    }

    fun obtenerUbicacion() {
        val permisoPreciso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val permisoAproximado = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!permisoPreciso && !permisoAproximado) {
            mensajeUbicacion = "Debes autorizar el acceso a la ubicación"
            return
        }

        val administrador = context.getSystemService(Context.LOCATION_SERVICE)
                as LocationManager
        val proveedor = when {
            permisoPreciso && administrador.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                LocationManager.GPS_PROVIDER
            administrador.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (proveedor == null) {
            mensajeUbicacion = "Activa la ubicación del dispositivo e inténtalo nuevamente"
            return
        }

        cancelacionUbicacion?.cancel()
        val cancelacion = CancellationSignal()
        cancelacionUbicacion = cancelacion
        buscandoUbicacion = true
        mensajeUbicacion = "Buscando ubicación…"

        try {
            LocationManagerCompat.getCurrentLocation(
                administrador,
                proveedor,
                cancelacion,
                ContextCompat.getMainExecutor(context)
            ) { ubicacion ->
                buscandoUbicacion = false
                cancelacionUbicacion = null
                if (ubicacion != null) {
                    viewModel.onUbicacionObtenida(
                        ubicacion.latitude,
                        ubicacion.longitude
                    )
                    mensajeUbicacion = "Ubicación obtenida correctamente"
                } else {
                    mensajeUbicacion = "No se pudo obtener la ubicación. Inténtalo nuevamente"
                }
            }
        } catch (e: SecurityException) {
            buscandoUbicacion = false
            mensajeUbicacion = "No se concedió permiso de ubicación"
        } catch (e: Exception) {
            buscandoUbicacion = false
            mensajeUbicacion = "No se pudo consultar la ubicación del dispositivo"
        }
    }

    val permisosUbicacionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            obtenerUbicacion()
        } else {
            mensajeUbicacion = "Permiso denegado. Puedes guardar el gasto sin ubicación"
        }
    }

    val calendario = rememberDatePickerState()

    // Cámara: guarda una fotografía completa en un archivo interno
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { tomada ->
        val uri = uriFotoPendiente

        if (tomada && uri != null) {
            viewModel.onComprobanteSeleccionado(uri.toString())
            errorFoto = null
        } else {
            errorFoto = "No se capturó ninguna fotografía"
        }

        uriFotoPendiente = null
    }

    // Galería: utiliza el selector de fotografías de Android
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val carpeta = File(context.filesDir, "comprobantes")
                if (!carpeta.exists()) carpeta.mkdirs()

                val archivo = File(
                    carpeta,
                    "comprobante_${System.currentTimeMillis()}.jpg"
                )

                context.contentResolver.openInputStream(uri)?.use { entrada ->
                    archivo.outputStream().use { salida ->
                        entrada.copyTo(salida)
                    }
                } ?: throw IllegalStateException(
                    "No se pudo abrir la imagen seleccionada"
                )

                viewModel.onComprobanteSeleccionado(
                    Uri.fromFile(archivo).toString()
                )
                errorFoto = null

            } catch (e: Exception) {
                errorFoto = "No se pudo guardar la imagen seleccionada"
            }
        }
    }

    val proveedorEncontrado =
        buscarProveedor(normalizarRut(estado.rutEmisor))

    LaunchedEffect(estado.rutEmisor, proveedorEncontrado) {
        if (
            proveedorEncontrado != null &&
            estado.nombreProveedor != proveedorEncontrado
        ) {
            viewModel.onNombreProveedorChange(proveedorEncontrado)
        }
    }

    val hayDatos = estado.tipoDocumento.isNotBlank() ||
            estado.numeroDocumento.isNotBlank() ||
            estado.rutEmisor.isNotBlank() ||
            estado.nombreProveedor.isNotBlank() ||
            estado.fechaDocumento.isNotBlank() ||
            estado.monto.isNotBlank() ||
            estado.motivoReembolso.isNotBlank() ||
            estado.comprobanteUri.isNotBlank() ||
            estado.latitud != null || estado.longitud != null

    fun volver() {
        if (hayDatos) {
            advertirSalida = true
        } else {
            onVolver()
        }
    }

    BackHandler { volver() }

    val formatoMoneda = NumberFormat.getNumberInstance(
        Locale("es", "CL")
    )

    val tope = when (categoria) {
        "Estacionamiento" -> 30000
        "Peajes" -> 50000
        "Alimentación" -> 50000
        "Materiales" -> 100000
        else -> 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TextButton(onClick = { volver() }) {
            Text("← Volver", color = Azul)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (esEdicion) {
                    "Editar gasto"
                } else if (esReembolso) {
                    "Registrar reembolso"
                } else {
                    "Registrar gasto"
                },
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Azul
            )

            Text(
                text = "TeqGastos",
                fontSize = 13.sp,
                color = Verde
            )
        }

        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    proyecto,
                    fontWeight = FontWeight.Bold,
                    color = Texto
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    categoria,
                    fontWeight = FontWeight.SemiBold,
                    color = Verde
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    if (esReembolso) {
                        "Gasto excepcional sujeto a revisión"
                    } else {
                        "Fondo por Rendir"
                    },
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                if (!esReembolso && tope > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tope referencial: $ ${formatoMoneda.format(tope)}",
                        color = Verde,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text(
                    "Documento",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Texto
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Boleta", "Factura").forEach { tipo ->
                        val seleccionado =
                            estado.tipoDocumento == tipo

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.onTipoDocumentoSeleccionado(tipo)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (seleccionado) {
                                    Color(0xFFE9F5F3)
                                } else {
                                    Fondo
                                }
                            )
                        ) {
                            Text(
                                tipo,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                color = if (seleccionado) Verde else Texto,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                estado.errorTipoDocumento?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = estado.numeroDocumento,
                    onValueChange = viewModel::onNumeroDocumentoChange,
                    label = { Text("Número de documento") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorNumeroDocumento != null,
                    supportingText = {
                        estado.errorNumeroDocumento?.let { Text(it) }
                    }
                )

                OutlinedTextField(
                    value = estado.rutEmisor,
                    onValueChange = { rut ->
                        viewModel.onRutEmisorChange(rut)

                        val encontrado =
                            buscarProveedor(normalizarRut(rut))

                        if (encontrado != null) {
                            viewModel.onNombreProveedorChange(encontrado)
                        } else if (proveedorEncontrado != null) {
                            viewModel.onNombreProveedorChange("")
                        }
                    },
                    label = { Text("RUT del proveedor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorRutEmisor != null,
                    supportingText = {
                        estado.errorRutEmisor?.let { Text(it) }
                    }
                )

                if (proveedorEncontrado != null) {
                    Text(
                        "✓ Proveedor encontrado en registros anteriores",
                        color = Verde,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = estado.nombreProveedor,
                    onValueChange = viewModel::onNombreProveedorChange,
                    label = { Text("Razón social del proveedor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = proveedorEncontrado != null,
                    isError = estado.errorNombreProveedor != null,
                    supportingText = {
                        estado.errorNombreProveedor?.let { Text(it) }
                    }
                )

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { mostrarCalendario = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        estado.fechaDocumento.ifBlank {
                            "Seleccionar fecha del documento"
                        }
                    )
                }

                estado.errorFechaDocumento?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = estado.monto,
                    onValueChange = viewModel::onMontoChange,
                    label = { Text("Monto del gasto ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    isError = estado.errorMonto != null,
                    supportingText = {
                        estado.errorMonto?.let { Text(it) }
                    }
                )
            }
        }

        if (esReembolso) {
            Spacer(Modifier.height(14.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        "Justificación del reembolso",
                        fontWeight = FontWeight.Bold,
                        color = Texto,
                        fontSize = 17.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Explique por qué fue necesario realizar este gasto.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = estado.motivoReembolso,
                        onValueChange = viewModel::onMotivoReembolsoChange,
                        label = { Text("Motivo obligatorio") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        isError = estado.errorMotivoReembolso != null,
                        supportingText = {
                            estado.errorMotivoReembolso?.let { Text(it) }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // NUEVA SECCIÓN: FOTOGRAFÍA DEL COMPROBANTE
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text(
                    "Fotografía del comprobante",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Texto
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Adjunte una fotografía de la boleta o factura.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        try {
                            val carpeta = File(
                                context.filesDir,
                                "comprobantes"
                            )

                            if (!carpeta.exists()) {
                                carpeta.mkdirs()
                            }

                            val archivo = File(
                                carpeta,
                                "comprobante_${System.currentTimeMillis()}.jpg"
                            )

                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                archivo
                            )

                            uriFotoPendiente = uri
                            camaraLauncher.launch(uri)

                        } catch (e: Exception) {
                            errorFoto = "No se pudo abrir la cámara"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Azul
                    )
                ) {
                    Text("Tomar fotografía")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        galeriaLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seleccionar desde galería")
                }

                if (estado.comprobanteUri.isNotBlank()) {

                    Spacer(Modifier.height(12.dp))

                    val imagen = remember(estado.comprobanteUri) {
                        try {
                            val uri = Uri.parse(estado.comprobanteUri)

                            val bitmap = if (
                                Build.VERSION.SDK_INT >=
                                Build.VERSION_CODES.P
                            ) {
                                val source =
                                    ImageDecoder.createSource(
                                        context.contentResolver,
                                        uri
                                    )
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                context.contentResolver
                                    .openInputStream(uri)?.use {
                                        BitmapFactory.decodeStream(it)
                                    }
                            }

                            bitmap?.asImageBitmap()

                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (imagen != null) {
                        Image(
                            bitmap = imagen,
                            contentDescription = "Comprobante adjunto",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            "No se pudo mostrar la fotografía",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    TextButton(
                        onClick = {
                            viewModel.quitarComprobante()
                        }
                    ) {
                        Text(
                            "Quitar fotografía",
                            color = Color(0xFFB3261E)
                        )
                    }
                }

                errorFoto?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))

                Text(
                    "Ubicación del gasto",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Texto
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Puedes registrar la ubicación donde realizaste el gasto.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val preciso = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val aproximado = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (preciso || aproximado) {
                            obtenerUbicacion()
                        } else {
                            permisosUbicacionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    enabled = !buscandoUbicacion,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Azul)
                ) {
                    Text(if (buscandoUbicacion) "Obteniendo ubicación…" else "Obtener ubicación")
                }

                Spacer(Modifier.height(8.dp))

                if (estado.latitud != null && estado.longitud != null) {
                    Text(
                        "Latitud: ${"%.6f".format(Locale.US, estado.latitud)}",
                        color = Verde
                    )
                    Text(
                        "Longitud: ${"%.6f".format(Locale.US, estado.longitud)}",
                        color = Verde
                    )
                    TextButton(onClick = {
                        cancelacionUbicacion?.cancel()
                        buscandoUbicacion = false
                        viewModel.quitarUbicacion()
                        mensajeUbicacion = null
                    }) {
                        Text("Quitar ubicación", color = Color(0xFFB3261E))
                    }
                } else {
                    Text("Ubicación aún no registrada", color = Color.Gray)
                }

                mensajeUbicacion?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = Texto, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (viewModel.validarGasto(esReembolso)) {
                    mostrarConfirmacion = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Azul
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                if (esEdicion) "Guardar cambios" else "Validar gasto",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(20.dp))
    }

    if (mostrarCalendario) {
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        calendario.selectedDateMillis?.let {
                            val formato = SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            )
                            formato.timeZone =
                                TimeZone.getTimeZone("UTC")

                            viewModel.onFechaDocumentoChange(
                                formato.format(Date(it))
                            )
                        }
                        mostrarCalendario = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarCalendario = false }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = calendario)
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = {
                Text(
                    if (esEdicion) "Confirmar cambios"
                    else "Confirmar gasto"
                )
            },
            text = {
                Column {
                    Text("Categoría: $categoria")
                    Text("Proveedor: ${estado.nombreProveedor}")
                    Text("Monto: $ ${estado.monto}")

                    if (estado.comprobanteUri.isNotBlank()) {
                        Text("✓ Fotografía adjunta")
                    }

                    if (estado.latitud != null && estado.longitud != null) {
                        Text("✓ Ubicación registrada")
                    }

                    if (esReembolso) {
                        Spacer(Modifier.height(8.dp))
                        Text("Motivo: ${estado.motivoReembolso}")
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        if (esEdicion) {
                            "¿Desea guardar los cambios realizados?"
                        } else {
                            "¿Desea confirmar este registro?"
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        onConfirmar(estado.copy())
                    }
                ) {
                    Text(
                        if (esEdicion) "Guardar" else "Confirmar",
                        color = Verde
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmacion = false }
                ) {
                    Text("Volver al formulario")
                }
            }
        )
    }

    if (advertirSalida) {
        AlertDialog(
            onDismissRequest = { advertirSalida = false },
            title = { Text("Volver a la pantalla anterior") },
            text = {
                Text(
                    "Hay información ingresada. " +
                            "Si vuelve, los cambios aún no confirmados " +
                            "podrían perderse."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        advertirSalida = false
                        onVolver()
                    }
                ) {
                    Text("Volver")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { advertirSalida = false }
                ) {
                    Text("Seguir editando")
                }
            }
        )
    }
}

