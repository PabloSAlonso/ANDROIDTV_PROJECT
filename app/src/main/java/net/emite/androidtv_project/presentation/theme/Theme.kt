package net.emite.androidtv_project.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/**
 * Configuración del esquema de colores para el tema oscuro.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    secondary = WhiteSecondary,
    background = DarkBackground
)

/**
 * Tema principal de la aplicación Android TV basado en Material 3.
 * 
 * @param isDarkTheme Indica si se debe aplicar el tema oscuro (por defecto siempre oscuro).
 * @param content El contenido composable al que se le aplicará el tema.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AndroidTVProjectTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
