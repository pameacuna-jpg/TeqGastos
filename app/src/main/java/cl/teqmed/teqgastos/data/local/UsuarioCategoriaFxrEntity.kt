
package cl.teqmed.teqgastos.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "usuario_categoria_fxr",

    primaryKeys = [
        "usuarioId",
        "categoriaId"
    ],

    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoriaFxrEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["categoriaId"])
    ]
)
data class UsuarioCategoriaFxrEntity(

    val usuarioId: Long,

    val categoriaId: Long,

    // Porcentaje autorizado para este trabajador.
    // Un valor de 0 significa que no puede usar la categoría.
    val porcentaje: Int = 0
)


