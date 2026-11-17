
package cl.teqmed.teqgastos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaFxrDao {

    // Obtener todas las categorías de TEQMED
    @Query("""
        SELECT * FROM categorias_fxr
        ORDER BY nombre ASC
    """)
    fun obtenerCategorias(): Flow<List<CategoriaFxrEntity>>

    // Obtener solamente categorías activas
    @Query("""
        SELECT * FROM categorias_fxr
        WHERE activa = 1
        ORDER BY nombre ASC
    """)
    fun obtenerCategoriasActivas(): Flow<List<CategoriaFxrEntity>>

    // Crear una nueva categoría
    @Insert
    suspend fun insertarCategoria(
        categoria: CategoriaFxrEntity
    ): Long

    // Modificar nombre o estado de una categoría
    @Update
    suspend fun actualizarCategoria(
        categoria: CategoriaFxrEntity
    )

    // Consultar porcentajes de un trabajador
    @Query("""
        SELECT * FROM usuario_categoria_fxr
        WHERE usuarioId = :usuarioId
    """)
    fun obtenerPorcentajesUsuario(
        usuarioId: Long
    ): Flow<List<UsuarioCategoriaFxrEntity>>

    // Guardar o actualizar un porcentaje individual
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPorcentaje(
        asignacion: UsuarioCategoriaFxrEntity
    )

    // Consultar el porcentaje de una categoría
    @Query("""
        SELECT porcentaje
        FROM usuario_categoria_fxr
        WHERE usuarioId = :usuarioId
        AND categoriaId = :categoriaId
        LIMIT 1
    """)
    suspend fun obtenerPorcentaje(
        usuarioId: Long,
        categoriaId: Long
    ): Int?

    // Calcular la suma de porcentajes del trabajador
    @Query("""
        SELECT COALESCE(SUM(porcentaje), 0)
        FROM usuario_categoria_fxr
        WHERE usuarioId = :usuarioId
    """)
    suspend fun sumarPorcentajes(
        usuarioId: Long
    ): Int

    // Eliminar asignaciones anteriores de un trabajador
    // para reemplazarlas por una nueva distribución
    @Query("""
        DELETE FROM usuario_categoria_fxr
        WHERE usuarioId = :usuarioId
    """)
    suspend fun eliminarPorcentajesUsuario(
        usuarioId: Long
    )
}
