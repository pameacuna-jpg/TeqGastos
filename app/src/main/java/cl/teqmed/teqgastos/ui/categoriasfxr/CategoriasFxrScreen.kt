
package cl.teqmed.teqgastos.ui.categoriasfxr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.teqmed.teqgastos.data.local.CategoriaFxrDao
import cl.teqmed.teqgastos.data.local.CategoriaFxrEntity
import kotlinx.coroutines.launch

@Composable
fun CategoriasFxrScreen(
    categoriaDao: CategoriaFxrDao,
    onVolver: () -> Unit
) {
    val categorias by categoriaDao.obtenerCategorias()
        .collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var nombreNueva by remember { mutableStateOf("") }
    var categoriaEditando by remember {
        mutableStateOf<CategoriaFxrEntity?>(null)
    }
    var nombreEditado by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Administración de categorías FXR")

        Spacer(Modifier.height(8.dp))

        Text(
            "Estas categorías son comunes para todos los trabajadores."
        )

        Spacer(Modifier.height(24.dp))

        Text("Agregar categoría")

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = nombreNueva,
            onValueChange = { nombreNueva = it },
            label = { Text("Nombre de la categoría") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val nombre = nombreNueva.trim()

                when {
                    nombre.isBlank() -> {
                        mensaje = "Ingrese el nombre de la categoría"
                    }

                    categorias.any {
                        it.nombre.equals(nombre, ignoreCase = true)
                    } -> {
                        mensaje = "Esta categoría ya existe"
                    }

                    else -> {
                        scope.launch {
                            categoriaDao.insertarCategoria(
                                CategoriaFxrEntity(
                                    nombre = nombre,
                                    activa = true
                                )
                            )
                            nombreNueva = ""
                            mensaje = "Categoría agregada correctamente"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar categoría")
        }

        if (mensaje.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(mensaje)
        }

        Spacer(Modifier.height(24.dp))

        HorizontalDivider()

        Spacer(Modifier.height(20.dp))

        Text("Categorías registradas")

        Spacer(Modifier.height(12.dp))

        if (categorias.isEmpty()) {
            Text("Todavía no existen categorías FXR.")
        }

        categorias.forEach { categoria ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(categoria.nombre)

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            if (categoria.activa) "Activa"
                            else "Desactivada"
                        )

                        Switch(
                            checked = categoria.activa,
                            onCheckedChange = { activa ->
                                scope.launch {
                                    categoriaDao.actualizarCategoria(
                                        categoria.copy(activa = activa)
                                    )
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            categoriaEditando = categoria
                            nombreEditado = categoria.nombre
                            mensaje = ""
                        }
                    ) {
                        Text("Editar nombre")
                    }

                    if (categoriaEditando?.id == categoria.id) {
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = nombreEditado,
                            onValueChange = { nombreEditado = it },
                            label = { Text("Nuevo nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val nuevoNombre = nombreEditado.trim()

                                when {
                                    nuevoNombre.isBlank() -> {
                                        mensaje = "El nombre no puede estar vacío"
                                    }

                                    categorias.any {
                                        it.id != categoria.id &&
                                                it.nombre.equals(
                                                    nuevoNombre,
                                                    ignoreCase = true
                                                )
                                    } -> {
                                        mensaje = "Ya existe otra categoría con ese nombre"
                                    }

                                    else -> {
                                        scope.launch {
                                            categoriaDao.actualizarCategoria(
                                                categoria.copy(
                                                    nombre = nuevoNombre
                                                )
                                            )
                                            categoriaEditando = null
                                            mensaje = "Categoría actualizada"
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Guardar cambios")
                        }

                        OutlinedButton(
                            onClick = {
                                categoriaEditando = null
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al administrador")
        }
    }
}


