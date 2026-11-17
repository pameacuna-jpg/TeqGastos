
package cl.teqmed.teqgastos.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            ALTER TABLE usuarios
            ADD COLUMN montoFxrAsignado INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}


