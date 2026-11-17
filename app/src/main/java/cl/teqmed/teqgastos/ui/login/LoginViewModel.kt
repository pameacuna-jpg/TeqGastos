package cl.teqmed.teqgastos.ui.login

import androidx.lifecycle.ViewModel
import cl.teqmed.teqgastos.data.local.UsuarioDao
import cl.teqmed.teqgastos.security.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.Locale

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private var rolAutenticado: String = ""
    private var idAutenticado: Long? = null

    fun onUsuarioChange(usuario: String) {
        _uiState.value = _uiState.value.copy(usuario = usuario, errorUsuario = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorPassword = null)
    }

    suspend fun validarLogin(usuarioDao: UsuarioDao): Boolean {
        val usuario = _uiState.value.usuario.trim().lowercase(Locale.ROOT)
        val password = _uiState.value.password
        rolAutenticado = ""
        idAutenticado = null

        if (usuario.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorUsuario = if (usuario.isBlank()) "Ingrese su usuario" else null,
                errorPassword = if (password.isBlank()) "Ingrese su contraseña" else null
            )
            return false
        }

        return try {
            val cuenta = withContext(Dispatchers.IO) {
                usuarioDao.obtenerPorNombreUsuario(usuario)
            }
            val valida = cuenta != null && cuenta.activo &&
                    withContext(Dispatchers.Default) {
                        PasswordUtils.verificarPassword(password, cuenta.passwordHash)
                    }

            if (!valida) {
                _uiState.value = _uiState.value.copy(
                    errorUsuario = "Usuario o contraseña incorrectos, o cuenta desactivada",
                    errorPassword = null
                )
                false
            } else {
                rolAutenticado = cuenta!!.rol
                idAutenticado = cuenta.id
                _uiState.value = _uiState.value.copy(
                    password = "", errorUsuario = null, errorPassword = null
                )
                true
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorUsuario = "No se pudo comprobar la cuenta. Intente nuevamente.",
                errorPassword = null
            )
            false
        }
    }

    fun obtenerRol(): String = rolAutenticado
    fun obtenerUsuarioId(): Long? = idAutenticado
}
