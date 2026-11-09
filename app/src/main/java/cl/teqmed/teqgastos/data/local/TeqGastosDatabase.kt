
package cl.teqmed.teqgastos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GastoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TeqGastosDatabase : RoomDatabase() {

    abstract fun gastoDao(): GastoDao
}
