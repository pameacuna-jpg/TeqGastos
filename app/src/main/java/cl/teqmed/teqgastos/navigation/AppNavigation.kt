package cl.teqmed.teqgastos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import cl.teqmed.teqgastos.ui.login.LoginScreen
import cl.teqmed.teqgastos.ui.login.LoginViewModel
import cl.teqmed.teqgastos.ui.proyecto.ProyectoScreen
import cl.teqmed.teqgastos.ui.proyecto.ProyectoViewModel
import cl.teqmed.teqgastos.ui.rendicion.RendicionScreen
import cl.teqmed.teqgastos.ui.rendicion.RendicionViewModel
import cl.teqmed.teqgastos.ui.fondorendir.FondoRendirScreen
import cl.teqmed.teqgastos.ui.fondorendir.FondoRendirViewModel
import cl.teqmed.teqgastos.ui.gastoregistro.GastoScreen
import cl.teqmed.teqgastos.ui.gastoregistro.GastoViewModel

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    // VIEWMODELS

    val loginViewModel = remember {
        LoginViewModel()
    }

    val proyectoViewModel = remember {
        ProyectoViewModel()
    }

    val rendicionViewModel = remember {
        RendicionViewModel()
    }

    val fondoRendirViewModel = remember {
        FondoRendirViewModel()
    }

    val gastoViewModel = remember {
        GastoViewModel()
    }

    // OBSERVAMOS LOS ESTADOS

    val proyectoUiState =
        proyectoViewModel.uiState.collectAsState().value

    val fondoRendirUiState =
        fondoRendirViewModel.uiState.collectAsState().value

    // NAVEGACIÓN

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // LOGIN

        composable(
            route = "login"
        ) {

            LoginScreen(
                viewModel = loginViewModel,
                onLoginCorrecto = {

                    navController.navigate(
                        route = "proyecto"
                    )
                }
            )
        }

        // SELECCIONAR PROYECTO

        composable(
            route = "proyecto"
        ) {

            ProyectoScreen(
                viewModel = proyectoViewModel,
                onContinuar = {

                    navController.navigate(
                        route = "rendicion"
                    )
                }
            )
        }

        // TIPO DE RENDICIÓN

        composable(
            route = "rendicion"
        ) {

            RendicionScreen(
                viewModel = rendicionViewModel,
                onContinuar = {

                    navController.navigate(
                        route = "fondorendir"
                    )
                }
            )
        }

        // FONDO POR RENDIR

        composable(
            route = "fondorendir"
        ) {

            FondoRendirScreen(
                viewModel = fondoRendirViewModel,
                proyecto = proyectoUiState.proyectoSeleccionado,
                onIngresarGasto = {

                    val categoria =
                        fondoRendirUiState.categoriaSeleccionada

                    navController.navigate(
                        route = "gasto/$categoria"
                    )
                }
            )
        }

        // REGISTRAR GASTO

        composable(
            route = "gasto/{categoria}"
        ) { backStackEntry ->

            val categoria =
                backStackEntry
                    .arguments
                    ?.getString("categoria")
                    ?: ""

            GastoScreen(
                viewModel = gastoViewModel,
                categoria = categoria,
                proyecto = proyectoUiState.proyectoSeleccionado
            )
        }
    }
}