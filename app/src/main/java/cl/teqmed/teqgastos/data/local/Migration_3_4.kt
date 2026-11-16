
package cl.teqmed.teqgastos.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {

    override fun migrate(db: SupportSQLiteDatabase) {

        // Tabla de usuarios
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nombre TEXT NOT NULL,
                apellido TEXT NOT NULL,
                nombreUsuario TEXT NOT NULL,
                passwordHash TEXT NOT NULL,
                correo TEXT NOT NULL,
                telefono TEXT NOT NULL,
                rol TEXT NOT NULL,
                activo INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Evitar nombres de usuario duplicados
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            index_usuarios_nombreUsuario
            ON usuarios(nombreUsuario)
            """.trimIndent()
        )

        // Identificación del técnico responsable
        db.execSQL(
            "ALTER TABLE rendiciones ADD COLUMN tecnicoId INTEGER"
        )

        // Identificación del supervisor que revisó
        db.execSQL(
            "ALTER TABLE rendiciones ADD COLUMN supervisorId INTEGER"
        )

        // Fecha de aprobación o rechazo
        db.execSQL(
            "ALTER TABLE rendiciones ADD COLUMN fechaRevision INTEGER"
        )
    }
}


