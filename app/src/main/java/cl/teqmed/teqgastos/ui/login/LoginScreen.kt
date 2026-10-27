package cl.teqmed.teqgastos.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.teqmed.teqgastos.ui.theme.TeqmedAzul
import cl.teqmed.teqgastos.ui.theme.TeqmedAzulClaro
import cl.teqmed.teqgastos.ui.theme.TeqmedVerde

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginCorrecto: () -> Unit
) {

    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Marca TEQMED
        Text(
            text = "TEQMED",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TeqmedAzul
        )

        Text(
            text = "TeqGastos",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TeqmedVerde
        )

        Text(
            text = "Rendición de gastos en terreno",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = TeqmedAzulClaro
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Ingreso al sistema",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = uiState.usuario,
                    onValueChange = {
                        viewModel.onUsuarioChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Usuario")
                    },
                    singleLine = true,
                    isError = uiState.errorUsuario != null,
                    supportingText = {
                        if (uiState.errorUsuario != null) {
                            Text(
                                text = uiState.errorUsuario
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = {
                        viewModel.onPasswordChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Contraseña")
                    },
                    singleLine = true,
                    isError = uiState.errorPassword != null,
                    supportingText = {
                        if (uiState.errorPassword != null) {
                            Text(
                                text = uiState.errorPassword
                            )
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (viewModel.validarLogin()) {
                            onLoginCorrecto()
                        }
                    }
                ) {
                    Text(text = "Ingresar")
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "TEQMED · Soluciones que dan vida",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}