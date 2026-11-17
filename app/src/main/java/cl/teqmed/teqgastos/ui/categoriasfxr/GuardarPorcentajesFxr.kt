
package cl.teqmed.teqgastos.ui.categoriasfxr

import androidx.room.withTransaction
import cl.teqmed.teqgastos.data.local.TeqGastosDatabase
import cl.teqmed.teqgastos.data.local.UsuarioCategoriaFxrEntity
import kotlinx.coroutines.flow.first

suspend fun guardarPorcentajesFxr(
    database: TeqGastosDatabase,
    usuarioId: Long,
    porcentajes: Map<Long, Int>
): Boolean {

    // No permitir distribuciones vacías
    if (porcentajes.isEmpty()) {
        return false
    }

    // Cada porcentaje debe estar entre 0 y 100
    if (porcentajes.values.any { it !in 0..100 }) {
        return false
    }

    // La suma debe ser exactamente 100 %
    if (porcentajes.values.sum() != 100) {
        return false
    }

    return database.withTransaction {

        val dao = database.categoriaFxrDao()

        // Comprobar que el trabajador existe y está activo
        val usuario = database.usuarioDao()
            .obtenerPorId(usuarioId)

        if (usuario == null ||
            !usuario.activo ||
            usuario.rol != "TRABAJADOR"
        ) {
            return@withTransaction false
        }

        // Consultar el catálogo vigente
        val categorias = dao.obtenerCategorias()
            .first()

        val categoriasPorId = categorias.associateBy { it.id }

        // Todas las categorías enviadas deben existir y estar activas
        val categoriasValidas = porcentajes.keys.all { categoriaId ->
            categoriasPorId[categoriaId]?.activa == true
        }

        if (!categoriasValidas) {
            return@withTransaction false
        }

        // Reemplazar la distribución anterior dentro
        // de la misma transacción
        dao.eliminarPorcentajesUsuario(usuarioId)

        porcentajes.forEach { (categoriaId, porcentaje) ->

            dao.guardarPorcentaje(
                UsuarioCategoriaFxrEntity(
                    usuarioId = usuarioId,
                    categoriaId = categoriaId,
                    porcentaje = porcentaje
                )
            )
        }

        true
    }
}
