package cl.teqmed.teqgastos.ui.usuarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.teqmed.teqgastos.data.local.CategoriaFxrDao
import cl.teqmed.teqgastos.data.local.UsuarioEntity
import kotlinx.coroutines.flow.combine
import java.text.NumberFormat
import java.util.Locale

private val AzulEdicion = Color(0xFF08638B)
private val FondoEdicion = Color(0xFFF5F8FA)

@Composable
fun EditarUsuarioScreen(
    usuario: UsuarioEntity,
    categoriaDao: CategoriaFxrDao,
    onVolver: () -> Unit,
    onGuardar: (UsuarioEntity, Map<Long, Int>?) -> Unit
) {
    var nombre by remember(usuario.id) { mutableStateOf(usuario.nombre) }
    var apellido by remember(usuario.id) { mutableStateOf(usuario.apellido) }
    var correo by remember(usuario.id) { mutableStateOf(usuario.correo) }
    var telefono by remember(usuario.id) { mutableStateOf(usuario.telefono) }
    var rol by remember(usuario.id) { mutableStateOf(usuario.rol) }
    var montoFxr by remember(usuario.id) { mutableStateOf(usuario.montoFxrAsignado.toString()) }
    var error by remember(usuario.id) { mutableStateOf("") }
    val esAdministradorPrincipal = usuario.nombreUsuario == "administrador"
    val formato = remember { NumberFormat.getNumberInstance(Locale("es", "CL")) }

    // Esperamos que ambas consultas emitan antes de inicializar los campos.
    val configuracion by remember(categoriaDao, usuario.id) {
        combine(
            categoriaDao.obtenerCategoriasActivas(),
            categoriaDao.obtenerPorcentajesUsuario(usuario.id)
        ) { categorias, asignaciones ->
            categorias to asignaciones
        }
    }.collectAsState(initial = null)

    var porcentajesTexto by remember(usuario.id) {
        mutableStateOf<Map<Long, String>>(emptyMap())
    }
    var porcentajesInicializados by remember(usuario.id) { mutableStateOf(false) }

    LaunchedEffect(usuario.id, configuracion) {
        val datos = configuracion
        if (!porcentajesInicializados && datos != null) {
            val anteriores = datos.second.associate { it.categoriaId to it.porcentaje }
            porcentajesTexto = datos.first.associate { categoria ->
                categoria.id to (anteriores[categoria.id] ?: 0).toString()
            }
            porcentajesInicializados = true
        }
    }

    val categorias = configuracion?.first.orEmpty()
    val porcentajes = categorias.associate { categoria ->
        categoria.id to (porcentajesTexto[categoria.id]?.toIntOrNull() ?: 0)
    }
    val totalPorcentaje = porcentajes.values.sum()
    val montoNumerico = montoFxr.toLongOrNull() ?: 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoEdicion)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onVolver) { Text("← Volver", color = AzulEdicion) }
        Spacer(Modifier.height(12.dp))
        Text("Editar usuario", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = AzulEdicion)
        Spacer(Modifier.height(8.dp))
        Text("Modificar datos, permisos y fondo asignado", color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = usuario.nombreUsuario,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre de usuario") },
            enabled = false
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = nombre, onValueChange = { nombre = it },
            modifier = Modifier.fillMaxWidth(), label = { Text("Nombre") }, singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = apellido, onValueChange = { apellido = it },
            modifier = Modifier.fillMaxWidth(), label = { Text("Apellido") }, singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            modifier = Modifier.fillMaxWidth(), label = { Text("Correo electrónico") }, singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = telefono, onValueChange = { telefono = it },
            modifier = Modifier.fillMaxWidth(), label = { Text("Teléfono") }, singleLine = true
        )

        if (rol == "TRABAJADOR") {
            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("Fondo por Rendir (FXR)", fontSize = 19.sp,
                fontWeight = FontWeight.Bold, color = AzulEdicion)
            Spacer(Modifier.height(8.dp))
            Text("Monto fijo autorizado por TEQMED para este trabajador.",
                color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = montoFxr,
                onValueChange = { nuevo -> if (nuevo.all { it.isDigit() }) montoFxr = nuevo },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monto FXR asignado (CLP)") },
                placeholder = { Text("Ejemplo: 150000") },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Text("Ejemplo: ingresa 150000 para asignar $150.000.",
                color = Color.Gray, fontSize = 12.sp)

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("Distribución por categorías FXR", fontSize = 19.sp,
                fontWeight = FontWeight.Bold, color = AzulEdicion)
            Spacer(Modifier.height(8.dp))
            Text("Los porcentajes de este trabajador deben sumar exactamente 100 %.",
                color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))

            if (configuracion == null || !porcentajesInicializados) {
                CircularProgressIndicator()
            } else if (categorias.isEmpty()) {
                Text("No hay categorías activas. Primero créalas desde Administración de categorías FXR.",
                    color = Color.Red)
            } else {
                categorias.forEach { categoria ->
                    val porcentaje = porcentajes[categoria.id] ?: 0
                    val limite = montoNumerico * porcentaje / 100L
                    OutlinedTextField(
                        value = porcentajesTexto[categoria.id] ?: "0",
                        onValueChange = { nuevo ->
                            if (nuevo.length <= 3 && nuevo.all { it.isDigit() }) {
                                porcentajesTexto = porcentajesTexto + (categoria.id to nuevo)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("${categoria.nombre} (%)") },
                        supportingText = {
                            Text(if (porcentaje == 0) "No autorizada (0 %)" else "Tope: $ ${formato.format(limite)}")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Text(
                    "Total distribuido: $totalPorcentaje %",
                    fontWeight = FontWeight.Bold,
                    color = if (totalPorcentaje == 100) Color(0xFF008267) else Color.Red
                )
                Spacer(Modifier.height(8.dp))
                Text("Una categoría con 0 % no podrá utilizarse para registrar gastos.",
                    color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        Text("Rol del usuario", fontWeight = FontWeight.Bold, color = AzulEdicion)
        listOf("TRABAJADOR", "SUPERVISOR", "ADMINISTRADOR").forEach { opcion ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = rol == opcion,
                    onClick = { if (!esAdministradorPrincipal) rol = opcion },
                    enabled = !esAdministradorPrincipal
                )
                Text(opcion)
            }
        }
        if (esAdministradorPrincipal) {
            Text("El rol del administrador principal está protegido.",
                color = Color.Gray, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))
        if (error.isNotEmpty()) {
            Text(error, color = Color.Red)
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = {
                val montoConvertido = montoFxr.toLongOrNull()
                when {
                    nombre.isBlank() || apellido.isBlank() ->
                        error = "Nombre y apellido son obligatorios."
                    rol == "TRABAJADOR" && montoConvertido == null ->
                        error = "Ingresa un monto FXR válido en pesos."
                    rol == "TRABAJADOR" && (!porcentajesInicializados || categorias.isEmpty()) ->
                        error = "Primero configura las categorías FXR."
                    rol == "TRABAJADOR" && categorias.any {
                        porcentajesTexto[it.id].isNullOrBlank() ||
                                (porcentajesTexto[it.id]?.toIntOrNull() ?: -1) !in 0..100
                    } -> error = "Cada porcentaje debe estar entre 0 y 100."
                    rol == "TRABAJADOR" && totalPorcentaje != 100 ->
                        error = "Los porcentajes deben sumar exactamente 100 %."
                    else -> {
                        error = ""
                        onGuardar(
                            usuario.copy(
                                nombre = nombre.trim(),
                                apellido = apellido.trim(),
                                correo = correo.trim(),
                                telefono = telefono.trim(),
                                rol = if (esAdministradorPrincipal) "ADMINISTRADOR" else rol,
                                montoFxrAsignado = if (rol == "TRABAJADOR")
                                    (montoConvertido ?: 0L) else usuario.montoFxrAsignado
                            ),
                            if (rol == "TRABAJADOR") porcentajes else null
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulEdicion)
        ) { Text("Guardar cambios") }
        Spacer(Modifier.height(20.dp))
    }
}
