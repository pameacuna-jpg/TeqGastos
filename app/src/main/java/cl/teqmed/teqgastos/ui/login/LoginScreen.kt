package cl.teqmed.teqgastos.ui.login

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val uiState = viewModel.uiState.collectAsState().value

    Text(text = "TeqGastos")

    OutlinedTextField(
        value = uiState.usuario,
        onValueChange = { viewModel.onUsuarioChange(it) },
        label = {
            Text(text = "Usuario")
        }
    )

    OutlinedTextField(
        value = uiState.password,
        onValueChange = { viewModel.onPasswordChange(it) },
        label = {
            Text(text = "Contraseña")
        }
    )
}