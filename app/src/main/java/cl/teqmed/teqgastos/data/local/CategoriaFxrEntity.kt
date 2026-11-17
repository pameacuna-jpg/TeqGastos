
package cl.teqmed.teqgastos.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categorias_fxr",
    indices = [
        Index(value = ["nombre"], unique = true)
    ]
)
data class CategoriaFxrEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val nombre: String,

    val activa: Boolean = true
)


