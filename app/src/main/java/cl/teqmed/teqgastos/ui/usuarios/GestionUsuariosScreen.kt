
package cl.teqmed.teqgastos.ui.usuarios

import androidx.compose.foundation.background
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
import cl.teqmed.teqgastos.data.local.UsuarioEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Azul = Color(0xFF08638B)
private val Verde = Color(0xFF008267)
private val Fondo = Color(0xFFF5F8FA)

@Composable
fun GestionUsuariosScreen(
    usuarios: List<UsuarioEntity>,
    onVolver: () -> Unit,
    onCrearUsuario: () -> Unit,
    onCambiarEstado: (UsuarioEntity) -> Unit,
    onEditarUsuario: (UsuarioEntity) -> Unit
) {
    var mostrarBusqueda by remember {
        mutableStateOf(false)
    }

    var textoBusqueda by remember {
        mutableStateOf("")
    }

    var filtroRol by remember {
        mutableStateOf("TODOS")
    }

    var filtroEstado by remember {
        mutableStateOf("TODOS")
    }

    val calendario = Calendar.getInstance()

    val inicioMes = (calendario.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val inicioMesSiguiente =
        (calendario.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
        }.timeInMillis

    val nombreMes = SimpleDateFormat(
        "MMMM yyyy",
        Locale("es", "CL")
    ).format(Date())

    val usuariosMes = usuarios.filter { usuario ->
        val fecha = usuario.fechaCreacion

        fecha != null &&
                fecha >= inicioMes &&
                fecha < inicioMesSiguiente
    }

    val usuariosFiltrados = if (!mostrarBusqueda) {
        usuariosMes
    } else {
        usuarios.filter { usuario ->

            val nombreCompleto =
                "${usuario.nombre} ${usuario.apellido}"

            val coincideTexto =
                textoBusqueda.isBlank() ||
                        nombreCompleto.contains(
                            textoBusqueda.trim(),
                            ignoreCase = true
                        ) ||
                        usuario.nombreUsuario.contains(
                            textoBusqueda.trim(),
                            ignoreCase = true
                        )

            val coincideRol =
                filtroRol == "TODOS" ||
                        usuario.rol.equals(
                            filtroRol,
                            ignoreCase = true
                        )

            val coincideEstado =
                filtroEstado == "TODOS" ||
                        (filtroEstado == "ACTIVOS" &&
                                usuario.activo) ||
                        (filtroEstado == "INACTIVOS" &&
                                !usuario.activo)

            coincideTexto &&
                    coincideRol &&
                    coincideEstado
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        TextButton(onClick = onVolver) {
            Text(
                "← Volver al administrador",
                color = Azul
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Administración de usuarios",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Azul
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Gestión de cuentas y permisos de TEQMED",
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onCrearUsuario,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Azul
            )
        ) {
            Text("+ Crear nuevo usuario")
        }

        Spacer(Modifier.height(24.dp))

        if (!mostrarBusqueda) {

            Text(
                text = "Usuarios creados este mes",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Azul
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = nombreMes.replaceFirstChar {
                    it.uppercase()
                },
                color = Color.Gray
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Usuarios del mes: ${usuariosMes.size}",
                color = Verde
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    mostrarBusqueda = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buscar todos los usuarios")
            }

        } else {

            Text(
                text = "Búsqueda de usuarios",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Azul
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = {
                    textoBusqueda = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Nombre o nombre de usuario")
                },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Filtrar por rol",
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            FiltroOpciones(
                opciones = listOf(
                    "TODOS",
                    "TRABAJADOR",
                    "SUPERVISOR",
                    "ADMINISTRADOR"
                ),
                seleccionado = filtroRol,
                onSeleccionar = {
                    filtroRol = it
                }
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Filtrar por estado",
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            FiltroOpciones(
                opciones = listOf(
                    "TODOS",
                    "ACTIVOS",
                    "INACTIVOS"
                ),
                seleccionado = filtroEstado,
                onSeleccionar = {
                    filtroEstado = it
                }
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    mostrarBusqueda = false
                    textoBusqueda = ""
                    filtroRol = "TODOS"
                    filtroEstado = "TODOS"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al mes actual")
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Resultados (${usuariosFiltrados.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Azul
        )

        Spacer(Modifier.height(12.dp))

        if (usuariosFiltrados.isEmpty()) {

            Text(
                text = if (mostrarBusqueda) {
                    "No se encontraron usuarios con esos filtros."
                } else {
                    "No hay usuarios registrados durante este mes. " +
                            "Puedes consultar las cuentas existentes " +
                            "en Buscar todos los usuarios."
                },
                color = Color.Gray
            )
        }

        usuariosFiltrados.forEach { usuario ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "${usuario.nombre} ${usuario.apellido}",
                        fontWeight = FontWeight.Bold,
                        color = Azul,
                        fontSize = 17.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Usuario: ${usuario.nombreUsuario}"
                    )

                    Text(
                        "Rol: ${usuario.rol}"
                    )

                    val fechaTexto =
                        usuario.fechaCreacion?.let { fecha ->
                            SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale("es", "CL")
                            ).format(Date(fecha))
                        } ?: "No registrada"

                    Text(
                        "Fecha de creación: $fechaTexto",
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = if (usuario.activo) {
                            "Cuenta activa"
                        } else {
                            "Cuenta desactivada"
                        },
                        color = if (usuario.activo) {
                            Verde
                        } else {
                            Color.Red
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            onEditarUsuario(usuario)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar usuario")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            onCambiarEstado(usuario)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled =
                            usuario.nombreUsuario != "administrador"
                    ) {
                        Text(
                            if (usuario.activo) {
                                "Desactivar cuenta"
                            } else {
                                "Activar cuenta"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroOpciones(
    opciones: List<String>,
    seleccionado: String,
    onSeleccionar: (String) -> Unit
) {
    Column {
        opciones.forEach { opcion ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                RadioButton(
                    selected = seleccionado == opcion,
                    onClick = {
                        onSeleccionar(opcion)
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Azul
                    )
                )

                Text(
                    text = opcion.lowercase()
                        .replaceFirstChar {
                            it.uppercase()
                        }
                )
            }
        }
    }
}
