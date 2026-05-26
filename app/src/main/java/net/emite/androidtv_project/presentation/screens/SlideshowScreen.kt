package net.emite.androidtv_project.presentation.screens

import android.util.Log
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import net.emite.androidtv_project.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import net.emite.androidtv_project.domain.model.MediaType
import net.emite.androidtv_project.presentation.theme.DarkBackground
import net.emite.androidtv_project.presentation.viewmodel.SlideshowUiState
import net.emite.androidtv_project.presentation.viewmodel.SlideshowViewModel
import net.emite.androidtv_project.presentation.slideshow.util.undeformed
import net.emite.androidtv_project.presentation.slideshow.SlideshowContainer
import net.emite.androidtv_project.presentation.slideshow.model.ScreenConfig
import net.emite.androidtv_project.presentation.slideshow.model.SlideMediaItem

/**
 * Pantalla principal del carrusel (slideshow).
 * Gestiona la visualización de medios, la orientación de la pantalla y el cierre de sesión mediante pulsación larga.
 * 
 * @param viewModel ViewModel que orquesta el estado del carrusel.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SlideshowScreen(
    screenConfig: ScreenConfig,
    viewModel: SlideshowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentItem by viewModel.currentItem.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var logoutJob by remember { mutableStateOf<Job?>(null) }

    // Solicita el foco para capturar eventos de teclado/mando
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Observar eventos de Toast del ViewModel para forzar la descarga del JSON
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .onKeyEvent { event ->
                if (event.key == Key.DirectionCenter || event.key == Key.Enter) {
                    if (event.type == KeyEventType.KeyDown) {
                        viewModel.onCenterClick()
                        true
                    } else {
                        false
                    }
                } else if (event.key == Key.Back || event.key == Key.Escape) {
                    // Gestión del cierre de sesión: mantener pulsado "ATRÁS" durante 3 segundos
                    when (event.type) {
                        KeyEventType.KeyDown -> {
                            if (logoutJob == null) {
                                logoutJob = scope.launch {
                                    delay(3000)
                                    viewModel.logout()
                                }
                            }
                            true
                        }

                        KeyEventType.KeyUp -> {
                            if (logoutJob?.isActive == true) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Mantén pulsado ATRÁS durante 3 segundos para salir",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                            logoutJob?.cancel()
                            logoutJob = null
                            true
                        }

                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is SlideshowUiState.Loading -> {
                SplashScreenContent(progress = null, message = state.message, screenConfig = screenConfig)
            }

            is SlideshowUiState.Preloading -> {
                val progress = if (state.total > 0) state.current.toFloat() / state.total.toFloat() else 0f
                SplashScreenContent(
                    progress = progress,
                    current = state.current,
                    total = state.total,
                    screenConfig = screenConfig
                )
            }

            is SlideshowUiState.Error -> {
                DeviceErrorScreen(
                    message = state.message,
                    deviceId = state.deviceId,
                    screenConfig = screenConfig
                )
            }

                is SlideshowUiState.Success -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val slideMediaItem = remember(currentItem) {
                            currentItem?.let {
                                if (it.type == MediaType.IMAGE) {
                                    SlideMediaItem.Image(uri = viewModel.getLocalUri(it))
                                } else {
                                    SlideMediaItem.Video(uri = viewModel.getLocalUri(it))
                                }
                            }
                        }

                        SlideshowContainer(
                            currentItem = slideMediaItem,
                            screenConfig = screenConfig,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Aviso de red no bloqueante (esquina superior derecha)
                    state.networkWarning?.let { warning ->
                        NetworkWarningBadge(message = warning, screenConfig = screenConfig)
                    }
                }
            }
        }
    }


/**
 * Contenido de la pantalla de bienvenida / precarga.
 * Muestra el banner corporativo y una barra de progreso de descargas.
 */
@Composable
fun SplashScreenContent(
    progress: Float? = null,
    current: Int? = null,
    total: Int? = null,
    message: String? = null,
    screenConfig: ScreenConfig
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Imagen corporativa a pantalla completa
        Image(
            painter = painterResource(id = R.drawable.wappa_banner_tv),
            contentDescription = "Wappa TV Splash",
            modifier = Modifier.fillMaxSize(),
            contentScale = if (isPortrait) ContentScale.Fit else ContentScale.Crop
        )

        // Capa de oscurecimiento para mejorar la lectura de los textos blancos
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(if (isPortrait) 0.8f else 0.4f)
                .undeformed(screenConfig),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (progress != null && current != null && total != null) {
                Text(
                    text = "Sincronizando contenidos...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.padding(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "$current / $total descargados",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                Text(
                    text = message ?: "Conectando con el servidor...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.padding(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

/**
 * Indicador visual discreto que informa sobre problemas de conectividad.
 */
@Composable
fun NetworkWarningBadge(message: String, screenConfig: ScreenConfig) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            color = Color(0xCC000000),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.undeformed(screenConfig)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Pantalla de error premium que muestra el código del dispositivo cuando no está registrado
 * en la instancia de Tegestiona indicada.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DeviceErrorScreen(
    message: String,
    deviceId: String?,
    screenConfig: ScreenConfig
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
            .undeformed(screenConfig),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .padding(32.dp)
        ) {

            if (deviceId != null) {
                // Caso: código no encontrado en Tegestiona — mostrar código de vinculación
                Text(
                    text = "⚠️ Dispositivo no registrado",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFFFCC00),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.padding(12.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.padding(20.dp))

                // Código de vinculación resaltado
                Surface(
                    color = Color(0xFF1A1A2E),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Código de vinculación",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFFFCC00).copy(alpha = 0.8f),
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(
                            text = deviceId,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.padding(12.dp))
                        Text(
                            text = "📄 Pulsa OK para copiarlo al portapapeles",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.45f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Copiar al portapapeles al pulsar OK / Enter en el mando
                LaunchedEffect(Unit) {
                    // Solo informativo — la copia se gestiona externamente si fuera necesario
                }

            } else {
                // Error genérico sin código de vinculación
                Text(
                    text = "❌ Error de conexión",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.padding(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
