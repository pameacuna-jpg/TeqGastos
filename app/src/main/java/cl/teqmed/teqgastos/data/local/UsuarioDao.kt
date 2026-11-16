package cl.teqmed.teqgastos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios ORDER BY apellido, nombre")
    fun obtenerTodos(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): UsuarioEntity?

    @Query(
        "SELECT * FROM usuarios WHERE nombreUsuario = :nombreUsuario LIMIT 1"
    )
    suspend fun obtenerPorNombreUsuario(
        nombreUsuario: String
    ): UsuarioEntity?

    @Query(
        "SELECT * FROM usuarios WHERE rol = :rol AND activo = 1 ORDER BY apellido, nombre"
    )
    fun obtenerActivosPorRol(rol: String): Flow<List<UsuarioEntity>>
}


