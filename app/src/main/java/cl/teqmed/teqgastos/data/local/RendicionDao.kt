package cl.teqmed.teqgastos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RendicionDao {

    @Insert
    suspend fun insertar(rendicion: RendicionEntity): Long

    @Update
    suspend fun actualizar(rendicion: RendicionEntity)

    @Query("SELECT * FROM rendiciones WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): RendicionEntity?

    @Query("SELECT * FROM rendiciones ORDER BY fechaCreacion DESC, id DESC")
    fun obtenerTodas(): Flow<List<RendicionEntity>>

    @Query(
        "SELECT * FROM rendiciones WHERE proyecto = :proyecto ORDER BY id DESC"
    )
    fun obtenerPorProyecto(proyecto: String): Flow<List<RendicionEntity>>

    @Query(
        """
        SELECT * FROM rendiciones
        WHERE proyecto = :proyecto
        AND tipoRendicion = :tipoRendicion
        AND estado = 'BORRADOR'
        ORDER BY id DESC
        LIMIT 1
        """
    )
    suspend fun obtenerBorrador(
        proyecto: String,
        tipoRendicion: String
    ): RendicionEntity?

    @Query(
        """
        UPDATE rendiciones
        SET estado = 'PENDIENTE_ENVIO',
            fechaFinalizacion = :fecha
        WHERE id = :rendicionId
        AND estado = 'BORRADOR'
        """
    )
    suspend fun finalizarRendicion(
        rendicionId: Long,
        fecha: Long
    ): Int
}
