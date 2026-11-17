
package cl.teqmed.teqgastos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        GastoEntity::class,
        RendicionEntity::class,
        UsuarioEntity::class,
        CategoriaFxrEntity::class,
        UsuarioCategoriaFxrEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class TeqGastosDatabase : RoomDatabase() {

    abstract fun gastoDao(): GastoDao

    abstract fun rendicionDao(): RendicionDao

    abstract fun usuarioDao(): UsuarioDao

    abstract fun categoriaFxrDao(): CategoriaFxrDao
}
