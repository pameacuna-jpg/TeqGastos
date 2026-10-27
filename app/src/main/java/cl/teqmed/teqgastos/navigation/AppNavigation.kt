package cl.teqmed.teqgastos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.teqmed.teqgastos.ui.login.LoginScreen
import cl.teqmed.teqgastos.ui.login.LoginViewModel
import cl.teqmed.teqgastos.ui.proyecto.ProyectoScreen
import cl.teqmed.teqgastos.ui.proyecto.ProyectoViewModel

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val loginViewModel = remember {
        LoginViewModel()
    }

    val proyectoViewModel = remember {
        ProyectoViewModel()
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {

            LoginScreen(
                viewModel = loginViewModel,
                onLoginCorrecto = {
                    navController.navigate("proyecto")
                }
            )
        }

        composable("proyecto") {

            ProyectoScreen(
                viewModel = proyectoViewModel
            )
        }
    }
}