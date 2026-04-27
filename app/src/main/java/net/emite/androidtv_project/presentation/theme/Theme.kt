package net.emite.androidtv_project.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    secondary = WhiteSecondary,
    background = DarkBackground
)

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
