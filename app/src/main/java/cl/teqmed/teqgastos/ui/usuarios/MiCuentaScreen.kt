package cl.teqmed.teqgastos.ui.usuarios

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.teqmed.teqgastos.data.local.UsuarioDao
import cl.teqmed.teqgastos.data.local.UsuarioEntity
import cl.teqmed.teqgastos.security.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val AzulCuenta = Color(0xFF08638B)
private val VerdeCuenta = Color(0xFF008267)
private val FondoCuenta = Color(0xFFF5F8FA)

@Composable
fun MiCuentaScreen(
    usuarioId: Long,
    usuarioDao: UsuarioDao,
    onVolver: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var cuenta by remember(usuarioId) { mutableStateOf<UsuarioEntity?>(null) }
    var nombre by remember(usuarioId) { mutableStateOf("") }
    var apellido by remember(usuarioId) { mutableStateOf("") }
    var correo by remember(usuarioId) { mutableStateOf("") }
    var telefono by remember(usuarioId) { mutableStateOf("") }
    var actual by remember(usuarioId) { mutableStateOf("") }
    var nueva by remember(usuarioId) { mutableStateOf("") }
    var repetir by remember(usuarioId) { mutableStateOf("") }
    var mensaje by remember(usuarioId) { mutableStateOf("") }
    var guardando by remember(usuarioId) { mutableStateOf(false) }

    LaunchedEffect(usuarioId) {
        val usuario = usuarioDao.obtenerPorId(usuarioId)
        cuenta = usuario
        if (usuario != null) {
            nombre = usuario.nombre
            apellido = usuario.apellido
            correo = usuario.correo
            telefono = usuario.telefono
        }
    }

    Column(
        Modifier.fillMaxSize().background(FondoCuenta)
            .verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        TextButton(onClick = onVolver) { Text("← Volver", color = AzulCuenta) }
        Spacer(Modifier.height(10.dp))
        Text("Mi cuenta", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = AzulCuenta)
        Spacer(Modifier.height(16.dp))
        val usuario = cuenta
        if (usuario == null) {
            Text("Cargando datos de la cuenta...")
        } else {
            val puedeEditarDatos = usuario.rol == "ADMINISTRADOR"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Datos personales", fontWeight = FontWeight.Bold, color = AzulCuenta)
                    Spacer(Modifier.height(10.dp))
                    Text("Usuario: ${usuario.nombreUsuario}")
                    Text("Rol: ${usuario.rol}")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(nombre, { if (puedeEditarDatos) nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true, readOnly = !puedeEditarDatos)
                    OutlinedTextField(apellido, { if (puedeEditarDatos) apellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth(), singleLine = true, readOnly = !puedeEditarDatos)
                    OutlinedTextField(correo, { if (puedeEditarDatos) correo = it }, label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth(), singleLine = true, readOnly = !puedeEditarDatos)
                    OutlinedTextField(telefono, { if (puedeEditarDatos) telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), singleLine = true, readOnly = !puedeEditarDatos)
                    if (!puedeEditarDatos) {
                        Spacer(Modifier.height(8.dp))
                        Text("Estos datos solo pueden ser modificados por Administración de usuarios.", color = AzulCuenta)
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("Cambiar contraseña (opcional)", fontWeight = FontWeight.Bold, color = AzulCuenta)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(actual, { actual = it }, label = { Text("Contraseña actual") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(nueva, { nueva = it }, label = { Text("Contraseña nueva") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(repetir, { repetir = it }, label = { Text("Repetir contraseña nueva") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(12.dp))
                    if (mensaje.isNotBlank()) {
                        Text(mensaje, color = if (mensaje == "Cambios guardados correctamente") VerdeCuenta else Color(0xFFB3261E))
                        Spacer(Modifier.height(10.dp))
                    }
                    Button(
                        enabled = !guardando,
                        onClick = {
                            if (puedeEditarDatos && (nombre.isBlank() || apellido.isBlank())) {
                                mensaje = "Nombre y apellido son obligatorios"
                            } else if (puedeEditarDatos && correo.isNotBlank() && (!correo.contains("@") || !correo.substringAfter("@").contains("."))) {
                                mensaje = "Ingrese un correo válido"
                            } else if (!puedeEditarDatos && nueva.isBlank() && actual.isBlank() && repetir.isBlank()) {
                                mensaje = "Ingrese una nueva contraseña para guardar cambios"
                            } else if (listOf(actual, nueva, repetir).any { it.isNotBlank() } &&
                                (actual.isBlank() || nueva.length < 8 || nueva != repetir)) {
                                mensaje = "Para cambiar contraseña ingrese la actual y una nueva de al menos 8 caracteres, repetida correctamente"
                            } else {
                                guardando = true
                                mensaje = ""
                                scope.launch {
                                    try {
                                        val existente = usuarioDao.obtenerPorId(usuarioId)
                                        if (existente == null || !existente.activo) {
                                            mensaje = "La cuenta no está disponible"
                                        } else {
                                            val cambiarPassword = nueva.isNotBlank()
                                            val verificada = !cambiarPassword || withContext(Dispatchers.Default) {
                                                PasswordUtils.verificarPassword(actual, existente.passwordHash)
                                            }
                                            if (!verificada) {
                                                mensaje = "La contraseña actual es incorrecta"
                                            } else {
                                                val hash = if (cambiarPassword) withContext(Dispatchers.Default) {
                                                    PasswordUtils.crearHash(nueva)
                                                } else existente.passwordHash
                                                val actualizada = existente.copy(
                                                    nombre = if (existente.rol == "ADMINISTRADOR") nombre.trim() else existente.nombre,
                                                    apellido = if (existente.rol == "ADMINISTRADOR") apellido.trim() else existente.apellido,
                                                    correo = if (existente.rol == "ADMINISTRADOR") correo.trim() else existente.correo,
                                                    telefono = if (existente.rol == "ADMINISTRADOR") telefono.trim() else existente.telefono,
                                                    passwordHash = hash
                                                )
                                                usuarioDao.actualizar(actualizada)
                                                cuenta = actualizada
                                                actual = ""
                                                nueva = ""
                                                repetir = ""
                                                mensaje = "Cambios guardados correctamente"
                                            }
                                        }
                                    } catch (e: Exception) {
                                        mensaje = "No se pudieron guardar los cambios"
                                    } finally {
                                        guardando = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulCuenta)
                    ) { Text(if (guardando) "Guardando..." else if (puedeEditarDatos) "Guardar cambios" else "Cambiar contraseña") }
                }
            }
        }
    }
}
