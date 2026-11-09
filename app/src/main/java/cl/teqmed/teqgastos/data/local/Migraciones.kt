package cl.teqmed.teqgastos.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            "ALTER TABLE gastos ADD COLUMN latitud REAL"
        )

        db.execSQL(
            "ALTER TABLE gastos ADD COLUMN longitud REAL"
        )
    }
}
