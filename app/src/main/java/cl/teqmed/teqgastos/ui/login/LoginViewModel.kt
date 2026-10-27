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

        val usuarioVacio = _uiState.value.usuario.isBlank()
        val passwordVacio = _uiState.value.password.isBlank()

        _uiState.value = _uiState.value.copy(
            errorUsuario = if (usuarioVacio) "Ingrese su usuario" else null,
            errorPassword = if (passwordVacio) "Ingrese su contraseña" else null
        )

        return !usuarioVacio && !passwordVacio
    }
}