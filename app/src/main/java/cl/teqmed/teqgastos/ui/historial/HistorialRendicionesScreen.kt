
package cl.teqmed.teqgastos.ui.historial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

private val AzulTeqmed = Color(0xFF08638B)
private val VerdeTeqmed = Color(0xFF008267)
private val FondoTeqmed = Color(0xFFF5F8FA)

data class RendicionHistorialUi(
    val id: Long,
    val proyecto: String,
    val tipo: String,
    val fecha: String,
    val estado: String,
    val total: Long
)

@Composable
fun HistorialRendicionesScreen(
    rendiciones: List<RendicionHistorialUi>,
    onVolver: () -> Unit
) {
    var rendicionSeleccionada by remember {
        mutableStateOf<RendicionHistorialUi?>(null)
    }

    val seleccionada = rendicionSeleccionada

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 28.dp,
                bottom = 16.dp
            )
    ) {
        TextButton(
            onClick = {
                if (seleccionada != null) {
                    rendicionSeleccionada = null
                } else {
                    onVolver()
                }
            }
        ) {
            Text("← Volver", color = AzulTeqmed)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = if (seleccionada == null)
                "Mis rendiciones"
            else
                "Rendición N.º ${seleccionada.id}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AzulTeqmed
        )

        Spacer(Modifier.height(20.dp))

        if (seleccionada != null) {
            DetalleRendicion(seleccionada)
        } else if (rendiciones.isEmpty()) {
            Text(
                text = "Todavía no hay rendiciones registradas.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = rendiciones,
                    key = { it.id }
                ) { rendicion ->
                    TarjetaRendicion(
                        rendicion = rendicion,
                        onClick = {
                            rendicionSeleccionada = rendicion
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaRendicion(
    rendicion: RendicionHistorialUi,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Rendición N.º ${rendicion.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AzulTeqmed
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${rendicion.proyecto} · ${rendicion.tipo}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Fecha: ${rendicion.fecha}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = nombreEstado(rendicion.estado),
                fontWeight = FontWeight.SemiBold,
                color = VerdeTeqmed
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Total: ${formatearMonto(rendicion.total)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AzulTeqmed
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tocar para ver detalle",
                color = AzulTeqmed,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun DetalleRendicion(
    rendicion: RendicionHistorialUi
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FondoTeqmed
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DatoDetalle("Proyecto", rendicion.proyecto)
            DatoDetalle("Tipo de rendición", rendicion.tipo)
            DatoDetalle("Fecha", rendicion.fecha)
            DatoDetalle("Estado", nombreEstado(rendicion.estado))

            HorizontalDivider()

            Text(
                text = "Total: ${formatearMonto(rendicion.total)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AzulTeqmed
            )
        }
    }
}

@Composable
private fun DatoDetalle(
    etiqueta: String,
    valor: String
) {
    Column {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelLarge,
            color = AzulTeqmed,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun nombreEstado(estado: String): String {
    return when (estado) {
        "BORRADOR" -> "En borrador"
        "PENDIENTE_ENVIO" -> "Pendiente de envío"
        "ENVIADA" -> "Enviada a revisión"
        "APROBADA" -> "Aprobada"
        "RECHAZADA" -> "Rechazada"
        else -> estado
    }
}

private fun formatearMonto(monto: Long): String {
    val formato = NumberFormat.getNumberInstance(
        Locale("es", "CL")
    )
    return "$ ${formato.format(monto)}"
}
