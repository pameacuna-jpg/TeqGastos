
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Azul = Color(0xFF08638B)
private val Verde = Color(0xFF008267)
private val Fondo = Color(0xFFF5F8FA)

data class NuevoUsuarioDatos(
    val nombre: String,
    val apellido: String,
    val nombreUsuario: String,
    val password: String,
    val correo: String,
    val telefono: String,
    val rol: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioScreen(
    onVolver: () -> Unit,
    onGuardar: (NuevoUsuarioDatos) -> Unit,
    errorExterno: String? = null,
    guardando: Boolean = false
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("TRABAJADOR") }
    var menuRoles by remember { mutableStateOf(false) }
    var mostrarPassword by remember { mutableStateOf(false) }
    var errorFormulario by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onVolver) {
            Text("← Volver a usuarios", color = Azul)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Crear nuevo usuario",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Azul
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Complete los datos de la cuenta TEQMED",
            color = Color.Gray
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(Modifier.padding(16.dp)) {

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = nombreUsuario,
                    onValueChange = { nombreUsuario = it },
                    label = { Text("Nombre de usuario *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña inicial *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation =
                        if (mostrarPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                mostrarPassword = !mostrarPassword
                            }
                        ) {
                            Text(
                                if (mostrarPassword) "Ocultar" else "Ver"
                            )
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Rol del usuario *",
                    fontWeight = FontWeight.SemiBold,
                    color = Azul
                )

                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = menuRoles,
                    onExpandedChange = { menuRoles = !menuRoles }
                ) {
                    OutlinedTextField(
                        value = when (rol) {
                            "TRABAJADOR" -> "Trabajador"
                            "SUPERVISOR" -> "Supervisor"
                            else -> "Administrador"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Seleccione el rol") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = menuRoles
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = menuRoles,
                        onDismissRequest = { menuRoles = false }
                    ) {
                        listOf(
                            "TRABAJADOR",
                            "SUPERVISOR",
                            "ADMINISTRADOR"
                        ).forEach { opcion ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        opcion.lowercase()
                                            .replaceFirstChar {
                                                it.uppercase()
                                            }
                                    )
                                },
                                onClick = {
                                    rol = opcion
                                    menuRoles = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        val mensajeError = errorFormulario ?: errorExterno

        if (mensajeError != null) {
            Text(
                text = mensajeError,
                color = Color(0xFFB3261E)
            )
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = {
                errorFormulario = when {
                    nombre.isBlank() ||
                            apellido.isBlank() ||
                            nombreUsuario.isBlank() ||
                            password.isBlank() ->
                        "Complete todos los campos obligatorios."

                    password.length < 8 ->
                        "La contraseña debe tener al menos 8 caracteres."

                    correo.isNotBlank() &&
                            !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(correo.trim()).matches() ->
                        "Ingrese un correo electrónico válido."

                    else -> null
                }

                if (errorFormulario == null) {
                    onGuardar(
                        NuevoUsuarioDatos(
                            nombre = nombre.trim(),
                            apellido = apellido.trim(),
                            nombreUsuario = nombreUsuario.trim()
                                .lowercase(),
                            password = password,
                            correo = correo.trim(),
                            telefono = telefono.trim(),
                            rol = rol
                        )
                    )
                }
            },
            enabled = !guardando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Verde
            )
        ) {
            Text(
                if (guardando) "Guardando..." else "Guardar usuario"
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

