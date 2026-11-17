
package cl.teqmed.teqgastos.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            ALTER TABLE usuarios
            ADD COLUMN fechaCreacion INTEGER
            """.trimIndent()
        )
    }
}


