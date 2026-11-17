
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.teqmed.teqgastos.data.local.UsuarioEntity

private val AzulEdicion = Color(0xFF08638B)
private val FondoEdicion = Color(0xFFF5F8FA)

@Composable
fun EditarUsuarioScreen(
    usuario: UsuarioEntity,
    onVolver: () -> Unit,
    onGuardar: (UsuarioEntity) -> Unit
) {
    var nombre by remember(usuario.id) {
        mutableStateOf(usuario.nombre)
    }

    var apellido by remember(usuario.id) {
        mutableStateOf(usuario.apellido)
    }

    var correo by remember(usuario.id) {
        mutableStateOf(usuario.correo)
    }

    var telefono by remember(usuario.id) {
        mutableStateOf(usuario.telefono)
    }

    var rol by remember(usuario.id) {
        mutableStateOf(usuario.rol)
    }

    var error by remember {
        mutableStateOf("")
    }

    val esAdministradorPrincipal =
        usuario.nombreUsuario == "administrador"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoEdicion)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        TextButton(onClick = onVolver) {
            Text("← Volver", color = AzulEdicion)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Editar usuario",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = AzulEdicion
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Modificar datos y permisos",
            color = Color.Gray
        )

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
            value = nombre,
            onValueChange = { nombre = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre") },
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Apellido") },
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Teléfono") },
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Rol del usuario",
            fontWeight = FontWeight.Bold,
            color = AzulEdicion
        )

        listOf(
            "TRABAJADOR",
            "SUPERVISOR",
            "ADMINISTRADOR"
        ).forEach { opcion ->

            Row(
                verticalAlignment =
                    androidx.compose.ui.Alignment.CenterVertically
            ) {

                RadioButton(
                    selected = rol == opcion,
                    onClick = {
                        if (!esAdministradorPrincipal) {
                            rol = opcion
                        }
                    },
                    enabled = !esAdministradorPrincipal
                )

                Text(opcion)
            }
        }

        if (esAdministradorPrincipal) {
            Text(
                "El rol del administrador principal está protegido.",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = Color.Red
            )

            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = {
                if (nombre.isBlank() || apellido.isBlank()) {
                    error = "Nombre y apellido son obligatorios."
                } else {
                    error = ""

                    onGuardar(
                        usuario.copy(
                            nombre = nombre.trim(),
                            apellido = apellido.trim(),
                            correo = correo.trim(),
                            telefono = telefono.trim(),
                            rol = if (esAdministradorPrincipal) {
                                "ADMINISTRADOR"
                            } else {
                                rol
                            }
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulEdicion
            )
        ) {
            Text("Guardar cambios")
        }
    }
}


