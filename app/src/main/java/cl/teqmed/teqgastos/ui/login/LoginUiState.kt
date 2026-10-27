package cl.teqmed.teqgastos.ui.login

data class LoginUiState(
    val usuario: String = "",
    val password: String = "",
    val errorUsuario: String? = null,
    val errorPassword: String? = null
)