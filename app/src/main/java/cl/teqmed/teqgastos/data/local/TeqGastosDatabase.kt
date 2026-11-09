package cl.teqmed.teqgastos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        GastoEntity::class,
        RendicionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TeqGastosDatabase : RoomDatabase() {

    abstract fun gastoDao(): GastoDao

    abstract fun rendicionDao(): RendicionDao
}
