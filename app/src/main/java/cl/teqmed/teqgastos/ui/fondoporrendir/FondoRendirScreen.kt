package cl.teqmed.teqgastos.ui.fondorendir

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import cl.teqmed.teqgastos.data.local.GastoDatabaseProvider
import cl.teqmed.teqgastos.data.local.CategoriaFxrEntity
import cl.teqmed.teqgastos.data.local.UsuarioCategoriaFxrEntity
import kotlinx.coroutines.flow.combine
import java.text.NumberFormat
import java.util.Locale

private val AzulTeqmed = Color(0xFF08638B)
private val VerdeTeqmed = Color(0xFF008267)
private val FondoTeqmed = Color(0xFFF5F8FA)
private val TextoPrincipal = Color(0xFF173E56)
private val TextoSecundario = Color(0xFF64748B)
private val BordeNormal = Color(0xFFDEE6EB)

@Composable
fun FondoRendirScreen(
    viewModel: FondoRendirViewModel,
    proyecto: String,
    onIngresarGasto: () -> Unit,
    onVolver: () -> Unit,
    usuarioId: Long? = null
) {
    BackHandler { onVolver() }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val categoriaDao = remember(context) {
        GastoDatabaseProvider.obtenerDatabase(context).categoriaFxrDao()
    }

    var cargando by remember(usuarioId) { mutableStateOf(true) }
    var categorias by remember(usuarioId) { mutableStateOf<List<CategoriaFxrEntity>>(emptyList()) }
    var porcentajes by remember(usuarioId) {
        mutableStateOf<Map<Long, Int>>(emptyMap())
    }

    LaunchedEffect(usuarioId, categoriaDao) {
        viewModel.actualizarCategoriasAutorizadas(emptySet())
        if (usuarioId == null) {
            cargando = false
            categorias = emptyList()
            porcentajes = emptyMap()
        } else {
            combine(
                categoriaDao.obtenerCategorias(),
                categoriaDao.obtenerPorcentajesUsuario(usuarioId)
            ) { catalogo: List<CategoriaFxrEntity>, asignaciones: List<UsuarioCategoriaFxrEntity> ->
                catalogo to asignaciones
            }.collect { (catalogo, asignaciones) ->
                categorias = catalogo.filter { it.activa }
                porcentajes = asignaciones.associate { it.categoriaId to it.porcentaje }
                viewModel.actualizarCategoriasAutorizadas(
                    catalogo.filter { it.activa && (porcentajes[it.id] ?: 0) > 0 }
                        .map { it.nombre }.toSet()
                )
                cargando = false
            }
        }
    }

    val formatoMoneda = remember { NumberFormat.getNumberInstance(Locale("es", "CL")) }
    val fondo = uiState.fondoAsignado.toLong()
    val categoriasAutorizadas = categorias.filter { (porcentajes[it.id] ?: 0) > 0 }

    Column(
        modifier = Modifier.fillMaxSize().background(FondoTeqmed)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        TextButton(onClick = onVolver) {
            Text("← Volver", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AzulTeqmed)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fondo por rendir", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AzulTeqmed)
            Text("TeqGastos", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VerdeTeqmed)
        }
        Spacer(Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(proyecto, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                    Text(uiState.correlativo, fontSize = 12.sp, color = TextoSecundario)
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Fondo asignado", fontSize = 12.sp, color = TextoSecundario)
                        Text("$ ${formatoMoneda.format(uiState.fondoAsignado)}", fontSize = 19.sp,
                            fontWeight = FontWeight.Bold, color = TextoPrincipal)
                    }
                    Column {
                        Text("Total rendido", fontSize = 12.sp, color = TextoSecundario)
                        Text("$ ${formatoMoneda.format(uiState.totalRendido)}", fontSize = 19.sp,
                            fontWeight = FontWeight.Bold, color = TextoPrincipal)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color(0xFFE9F5F3), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saldo disponible", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = TextoPrincipal)
                    Text("$ ${formatoMoneda.format(uiState.saldoDisponible)}", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, color = VerdeTeqmed)
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        Text("Categorías FXR", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
        Spacer(Modifier.height(12.dp))

        when {
            cargando -> CircularProgressIndicator(color = AzulTeqmed)
            usuarioId == null -> Text("No se pudo identificar al trabajador.", color = MaterialTheme.colorScheme.error)
            categorias.isEmpty() -> Text("Administración todavía no ha habilitado categorías FXR.", color = TextoSecundario)
            else -> {
                categorias.forEach { categoria ->
                    val porcentaje = porcentajes[categoria.id] ?: 0
                    val autorizado = porcentaje > 0
                    val tope = fondo * porcentaje / 100L
                    CategoriaOpcion(
                        texto = categoria.nombre,
                        tope = tope,
                        porcentaje = porcentaje,
                        habilitada = autorizado,
                        seleccionada = autorizado && uiState.categoriaSeleccionada == categoria.nombre,
                        onClick = { if (autorizado) viewModel.onCategoriaSeleccionada(categoria.nombre) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
                if (categoriasAutorizadas.isEmpty()) {
                    Text("No tienes categorías autorizadas. Solicita una asignación a Administración.",
                        color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        }

        uiState.errorCategoria?.let { mensaje ->
            Spacer(Modifier.height(12.dp))
            Text(mensaje, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = {
                if (viewModel.validarCategoria()) onIngresarGasto()
            },
            enabled = !cargando && usuarioId != null && categoriasAutorizadas.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulTeqmed)
        ) {
            Text("Ingresar gasto", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun CategoriaOpcion(
    texto: String,
    tope: Long,
    porcentaje: Int,
    habilitada: Boolean,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    val formatoMoneda = remember { NumberFormat.getNumberInstance(Locale("es", "CL")) }
    val colorFondo = if (seleccionada) Color(0xFFE9F5F3) else Color.White
    val colorBorde = if (seleccionada) VerdeTeqmed else BordeNormal
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(colorFondo, RoundedCornerShape(14.dp))
            .border(if (seleccionada) 2.dp else 1.dp, colorBorde, RoundedCornerShape(14.dp))
            .clickable(enabled = habilitada, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                color = if (habilitada) TextoPrincipal else TextoSecundario)
            Text(if (habilitada) "$porcentaje % autorizado" else "0 % · No autorizado",
                fontSize = 12.sp, color = TextoSecundario)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Tope", fontSize = 11.sp, color = TextoSecundario)
            Text("$ ${formatoMoneda.format(tope)}", fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (habilitada) TextoPrincipal else TextoSecundario)
        }
        Spacer(Modifier.width(12.dp))
        Text(if (seleccionada) "●" else "○", fontSize = 22.sp,
            color = if (seleccionada) VerdeTeqmed else Color(0xFFA5B2BA))
    }
}
