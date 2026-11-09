package cl.teqmed.teqgastos.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> = _uiState

    fun onUsuarioChange(usuario: String) {
        _uiState.value = _uiState.value.copy(
            usuario = usuario,
            errorUsuario = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorPassword = null
        )
    }

    fun validarLogin(): Boolean {

        val usuario = _uiState.value.usuario.trim().lowercase()
        val password = _uiState.value.password

        val usuarioVacio = usuario.isBlank()
        val passwordVacio = password.isBlank()

        if (usuarioVacio || passwordVacio) {
            _uiState.value = _uiState.value.copy(
                errorUsuario = if (usuarioVacio)
                    "Ingrese su usuario" else null,
                errorPassword = if (passwordVacio)
                    "Ingrese su contraseña" else null
            )
            return false
        }

        val credencialesValidas =
            (usuario == "trabajador" && password == "1234") ||
                    (usuario == "supervisor" && password == "1234")

        if (!credencialesValidas) {
            _uiState.value = _uiState.value.copy(
                errorUsuario = "Usuario o contraseña incorrectos",
                errorPassword = null
            )
            return false
        }

        _uiState.value = _uiState.value.copy(
            errorUsuario = null,
            errorPassword = null
        )

        return true
    }

    fun obtenerRol(): String {
        return when (_uiState.value.usuario.trim().lowercase()) {
            "supervisor" -> "SUPERVISOR"
            "trabajador" -> "TRABAJADOR"
            else -> ""
        }
    }
}
