package net.emite.androidtv_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import dagger.hilt.android.AndroidEntryPoint
import net.emite.androidtv_project.presentation.screens.SetupScreen
import net.emite.androidtv_project.presentation.screens.SlideshowScreen
import net.emite.androidtv_project.presentation.theme.AndroidTVProjectTheme
import net.emite.androidtv_project.presentation.viewmodel.MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTVProjectTheme {
                val hasInstance by mainViewModel.hasInstance.collectAsState()

                when (hasInstance) {
                    null -> {
                        // Pantalla de carga inicial mientras comprobamos la DB
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Cargando...")
                        }
                    }
                    false -> {
                        SetupScreen()
                    }
                    true -> {
                        // Pantalla del Slideshow activa
                        SlideshowScreen()
                    }
                }
            }
        }
    }
}
