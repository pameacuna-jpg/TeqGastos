package cl.teqmed.teqgastos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = TeqmedAzul,
    onPrimary = Blanco,

    secondary = TeqmedVerde,
    onSecondary = Blanco,

    tertiary = TeqmedVerde,

    background = FondoApp,
    onBackground = TextoPrincipal,

    surface = Blanco,
    onSurface = TextoPrincipal,

    error = ErrorRojo,
    onError = Blanco
)

private val DarkColorScheme = darkColorScheme(
    primary = TeqmedAzul,
    secondary = TeqmedVerde,
    tertiary = TeqmedVerde
)

@Composable
fun TeqGastosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}