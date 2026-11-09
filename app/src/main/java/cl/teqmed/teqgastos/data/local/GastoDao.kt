package cl.teqmed.teqgastos.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(gasto: GastoEntity): Long

    @Update
    suspend fun actualizar(gasto: GastoEntity)

    @Delete
    suspend fun eliminar(gasto: GastoEntity)

    // Consultar todos los gastos
    @Query("SELECT * FROM gastos ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<GastoEntity>>

    // Consultar un gasto por su ID
    @Query("SELECT * FROM gastos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): GastoEntity?

    // Consultar gastos de una rendición específica
    @Query(
        "SELECT * FROM gastos WHERE rendicionId = :rendicionId ORDER BY id DESC"
    )
    fun obtenerPorRendicion(
        rendicionId: Long
    ): Flow<List<GastoEntity>>

    // Eliminar todos los gastos
    @Query("DELETE FROM gastos")
    suspend fun eliminarTodos()
}
