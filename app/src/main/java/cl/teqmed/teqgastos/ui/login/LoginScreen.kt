package cl.teqmed.teqgastos.ui.login

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp




@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
    Text(text = "TeqGastos")

    OutlinedTextField(
        value = uiState.usuario,
        onValueChange = { viewModel.onUsuarioChange(it) },
        label = {
            Text(text = "Usuario")
        },
        isError = uiState.errorUsuario != null,
        supportingText = {
            if (uiState.errorUsuario != null) {
                Text(text = uiState.errorUsuario)
            }
        }
    )

    OutlinedTextField(
        value = uiState.password,
        onValueChange = { viewModel.onPasswordChange(it) },
        label = {
            Text(text = "Contraseña")
        },
        isError = uiState.errorPassword != null,
        supportingText = {
            if (uiState.errorPassword != null) {
                Text(text = uiState.errorPassword)
            }
        }
    )

    Button(
        onClick = {
            viewModel.validarLogin()
        }
    ) {
        Text(text = "Ingresar")
    }

  }
}