
package cl.teqmed.teqgastos.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.teqmed.teqgastos.R
import cl.teqmed.teqgastos.data.local.UsuarioDao
import kotlinx.coroutines.launch

private val AzulTeqmed = Color(0xFF08638B)
private val VerdeTeqmed = Color(0xFF008267)
private val FondoTeqmed = Color(0xFFF5F8FA)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    usuarioDao: UsuarioDao,
    onLoginCorrecto: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    val scope = rememberCoroutineScope()
    var mostrarPassword by remember { mutableStateOf(false) }
    var comprobando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoTeqmed)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.teqmed_logo_brochure
            ),
            contentDescription = "Logotipo TEQMED",
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(90.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "TeqGastos",
            fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold,
            color = VerdeTeqmed
        )

        Spacer(Modifier.height(26.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(Modifier.padding(20.dp)) {

                Text(
                    "Bienvenido",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulTeqmed
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    "Ingresa con tu cuenta corporativa",
                    fontSize = 15.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = uiState.usuario,
                    onValueChange = viewModel::onUsuarioChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Usuario") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.errorUsuario != null,
                    supportingText = {
                        uiState.errorUsuario?.let {
                            Text(it)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulTeqmed,
                        focusedLabelColor = AzulTeqmed
                    )
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation =
                        if (mostrarPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                mostrarPassword = !mostrarPassword
                            }
                        ) {
                            Text(
                                if (mostrarPassword) "Ocultar"
                                else "Ver",
                                color = AzulTeqmed
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.errorPassword != null,
                    supportingText = {
                        uiState.errorPassword?.let {
                            Text(it)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulTeqmed,
                        focusedLabelColor = AzulTeqmed
                    )
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (!comprobando) {
                            comprobando = true

                            scope.launch {
                                try {
                                    if (
                                        viewModel.validarLogin(usuarioDao)
                                    ) {
                                        onLoginCorrecto()
                                    }
                                } finally {
                                    comprobando = false
                                }
                            }
                        }
                    },
                    enabled = !comprobando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulTeqmed
                    )
                ) {
                    Text(
                        if (comprobando)
                            "Comprobando..."
                        else
                            "Ingresar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "TEQMED · Soluciones que dan vida",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
