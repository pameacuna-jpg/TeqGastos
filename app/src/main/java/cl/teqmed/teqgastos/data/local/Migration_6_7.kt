
package cl.teqmed.teqgastos.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {

    override fun migrate(db: SupportSQLiteDatabase) {

        // Categorías comunes para todos los trabajadores
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `categorias_fxr` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nombre` TEXT NOT NULL,
                `activa` INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Evitar nombres de categorías duplicados
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            `index_categorias_fxr_nombre`
            ON `categorias_fxr` (`nombre`)
            """.trimIndent()
        )

        // Porcentajes individuales por trabajador
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `usuario_categoria_fxr` (
                `usuarioId` INTEGER NOT NULL,
                `categoriaId` INTEGER NOT NULL,
                `porcentaje` INTEGER NOT NULL,
                PRIMARY KEY (`usuarioId`, `categoriaId`),
                FOREIGN KEY (`usuarioId`)
                    REFERENCES `usuarios` (`id`)
                    ON UPDATE NO ACTION
                    ON DELETE CASCADE,
                FOREIGN KEY (`categoriaId`)
                    REFERENCES `categorias_fxr` (`id`)
                    ON UPDATE NO ACTION
                    ON DELETE RESTRICT
            )
            """.trimIndent()
        )

        // Índice necesario para consultar categorías asignadas
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS
            `index_usuario_categoria_fxr_categoriaId`
            ON `usuario_categoria_fxr` (`categoriaId`)
            """.trimIndent()
        )
    }
}


