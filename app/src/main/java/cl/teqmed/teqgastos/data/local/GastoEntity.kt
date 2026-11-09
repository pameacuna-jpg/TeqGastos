package cl.teqmed.teqgastos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos")
data class GastoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Proyecto y clasificación
    val proyecto: String,
    val tipoRendicion: String,
    val categoria: String,

    // Datos del documento
    val tipoDocumento: String,
    val numeroDocumento: String,
    val rutEmisor: String,
    val nombreProveedor: String,
    val fechaDocumento: String,
    val monto: String,

    // Justificación del reembolso
    val motivoReembolso: String,

    // Fotografía del comprobante
    val comprobanteUri: String = "",

    // Geolocalización del gasto
    val latitud: Double? = null,
    val longitud: Double? = null
)
