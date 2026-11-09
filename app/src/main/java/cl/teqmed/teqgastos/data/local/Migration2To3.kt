package cl.teqmed.teqgastos.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {

    override fun migrate(db: SupportSQLiteDatabase) {

        // Crear la tabla de rendiciones
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS rendiciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                proyecto TEXT NOT NULL,
                tipoRendicion TEXT NOT NULL,
                estado TEXT NOT NULL,
                fechaCreacion INTEGER NOT NULL,
                fechaFinalizacion INTEGER,
                fechaEnvio INTEGER,
                observaciones TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Agregar el identificador de rendición a los gastos
        db.execSQL(
            "ALTER TABLE gastos ADD COLUMN rendicionId INTEGER"
        )

        // Crear rendiciones para los gastos existentes
        db.execSQL(
            """
            INSERT INTO rendiciones (
                proyecto,
                tipoRendicion,
                estado,
                fechaCreacion,
                fechaFinalizacion,
                fechaEnvio,
                observaciones
            )
            SELECT
                proyecto,
                tipoRendicion,
                'BORRADOR',
                0,
                NULL,
                NULL,
                ''
            FROM gastos
            GROUP BY proyecto, tipoRendicion
            """.trimIndent()
        )

        // Asociar los gastos antiguos a su rendición
        db.execSQL(
            """
            UPDATE gastos
            SET rendicionId = (
                SELECT r.id
                FROM rendiciones r
                WHERE r.proyecto = gastos.proyecto
                  AND r.tipoRendicion = gastos.tipoRendicion
                LIMIT 1
            )
            """.trimIndent()
        )
    }
}
