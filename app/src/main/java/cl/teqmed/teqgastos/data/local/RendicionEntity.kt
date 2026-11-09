
package cl.teqmed.teqgastos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rendiciones")
data class RendicionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val proyecto: String,

    val tipoRendicion: String,

    val estado: String = "BORRADOR",

    val fechaCreacion: Long = System.currentTimeMillis(),

    val fechaFinalizacion: Long? = null,

    val fechaEnvio: Long? = null,

    val observaciones: String = ""
)
