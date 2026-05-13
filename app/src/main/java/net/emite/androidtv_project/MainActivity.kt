package net.emite.androidtv_project

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import dagger.hilt.android.AndroidEntryPoint
import net.emite.androidtv_project.presentation.screens.SetupScreen
import net.emite.androidtv_project.presentation.screens.SlideshowScreen
import net.emite.androidtv_project.presentation.theme.AndroidTVProjectTheme
import net.emite.androidtv_project.presentation.theme.DarkBackground
import net.emite.androidtv_project.presentation.viewmodel.MainViewModel

/**
 * Actividad principal de la aplicación Android TV.
 * Se encarga de la navegación inicial entre la pantalla de configuración y el slideshow,
 * así como de gestionar la rotación dinámica de la interfaz.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Mantiene la pantalla encendida permanentemente mientras la app está activa
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            AndroidTVProjectTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground),
                    contentAlignment = Alignment.Center
                ) {
                    // Renderiza siempre a pantalla completa. La orientación física se gestiona
                    // desde SlideshowScreen con requestedOrientation de la Activity.
                    Box(modifier = Modifier.fillMaxSize()) {
                        val hasInstance by mainViewModel.hasInstance.collectAsState()
                        var showBootDebug by remember { mutableStateOf(false) }
                        var lastBackClickTime by remember { mutableLongStateOf(0L) }
                        var backClickCount by remember { mutableIntStateOf(0) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .onKeyEvent { event ->
                                    // Lógica para detectar 5 pulsaciones rápidas del botón "Back"
                                    // Esto podría usarse para menús de depuración ocultos.
                                    if (event.key == Key.Back && 
                                        event.type == KeyEventType.KeyUp) {
                                        val currentTime = System.currentTimeMillis()
                                        if (currentTime - lastBackClickTime < 1000L) {
                                            backClickCount++
                                            if (backClickCount >= 5) {
                                                showBootDebug = true
                                                backClickCount = 0
                                            }
                                        } else {
                                            backClickCount = 1
                                        }
                                        lastBackClickTime = currentTime
                                    }
                                    false
                                }
                        ) {
                            when (hasInstance) {
                                null -> {
                                    // Estado de carga inicial mientras se recupera la configuración local
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(DarkBackground),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.wappa_banner_tv),
                                                contentDescription = "Logo",
                                                modifier = Modifier.width(300.dp).padding(bottom = 24.dp)
                                            )
                                            Text(text = "Cargando...")
                                        }
                                    }
                                }
                                false -> {
                                    // Si no hay instancia configurada, ir a la pantalla de configuración
                                    SetupScreen()
                                }
                                true -> {
                                    // Si hay instancia, iniciar el carrusel de diapositivas
                                    SlideshowScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
