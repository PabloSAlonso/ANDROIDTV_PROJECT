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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import dagger.hilt.android.AndroidEntryPoint
import net.emite.androidtv_project.presentation.screens.BootDebugScreen
import net.emite.androidtv_project.presentation.screens.SetupScreen
import net.emite.androidtv_project.presentation.screens.SlideshowScreen
import net.emite.androidtv_project.presentation.theme.AndroidTVProjectTheme
import net.emite.androidtv_project.presentation.theme.DarkBackground
import net.emite.androidtv_project.presentation.viewmodel.MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            val config by mainViewModel.config.collectAsState()
            val configuration = LocalConfiguration.current
            
            val isVertical = config?.isVertical ?: false
            val isInverted = config?.isInverted ?: false
            
            AndroidTVProjectTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground),
                    contentAlignment = Alignment.Center
                ) {
                    // Contenedor que rota el contenido si es necesario para llenar toda la pantalla
                    Box(
                        modifier = if (isVertical) {
                            Modifier
                                .size(
                                    width = configuration.screenHeightDp.dp,
                                    height = configuration.screenWidthDp.dp
                                )
                                .graphicsLayer {
                                    rotationZ = if (isInverted) -90f else 90f
                                }
                        } else {
                            Modifier.fillMaxSize()
                        }
                    ) {
                        val hasInstance by mainViewModel.hasInstance.collectAsState()
                        var showBootDebug by remember { mutableStateOf(false) }
                        var lastBackClickTime by remember { mutableLongStateOf(0L) }
                        var backClickCount by remember { mutableIntStateOf(0) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .onKeyEvent { event ->
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
                                    SetupScreen()
                                }
                                true -> {
                                    SlideshowScreen()
                                }
                            }

                            if (showBootDebug) {
                                BootDebugScreen(
                                    onDismiss = { showBootDebug = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
