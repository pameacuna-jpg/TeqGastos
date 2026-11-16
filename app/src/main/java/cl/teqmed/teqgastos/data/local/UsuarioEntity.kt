
package cl.teqmed.teqgastos.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["nombreUsuario"], unique = true)
    ]
)
data class UsuarioEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nombre: String,

    val apellido: String,

    val nombreUsuario: String,

    val passwordHash: String,

    val correo: String = "",

    val telefono: String = "",

    val rol: String,

    val activo: Boolean = true
)


