package cl.teqmed.teqgastos.navigation

import androidx.compose.runtime.Composable
import cl.teqmed.teqgastos.ui.login.LoginScreen
import cl.teqmed.teqgastos.ui.login.LoginViewModel

@Composable
fun AppNavigation() {

    val loginViewModel = LoginViewModel()

    LoginScreen(
        viewModel = loginViewModel
    )
}