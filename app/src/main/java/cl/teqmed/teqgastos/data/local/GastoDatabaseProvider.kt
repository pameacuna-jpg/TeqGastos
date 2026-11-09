
package cl.teqmed.teqgastos.data.local

import android.content.Context
import androidx.room.Room

object GastoDatabaseProvider {

    @Volatile
    private var instancia: TeqGastosDatabase? = null

    fun obtenerDatabase(context: Context): TeqGastosDatabase {
        return instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                TeqGastosDatabase::class.java,
                "teqgastos_database"
            ).build().also {
                instancia = it
            }
        }
    }
}
