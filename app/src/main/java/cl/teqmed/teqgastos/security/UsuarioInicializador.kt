package cl.teqmed.teqgastos.security

import cl.teqmed.teqgastos.data.local.UsuarioDao
import cl.teqmed.teqgastos.data.local.UsuarioEntity

object UsuarioInicializador {

    suspend fun crearAdministradorInicial(
        usuarioDao: UsuarioDao
    ) {
        val existente = usuarioDao.obtenerPorNombreUsuario(
            "administrador"
        )

        if (existente != null) {
            return
        }

        val administrador = UsuarioEntity(
            nombre = "Administrador",
            apellido = "TEQMED",
            nombreUsuario = "administrador",
            passwordHash = PasswordUtils.crearHash("1234"),
            correo = "",
            telefono = "",
            rol = "ADMINISTRADOR",
            activo = true
        )

        usuarioDao.insertar(administrador)
    }
}


