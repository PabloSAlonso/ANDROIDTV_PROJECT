package net.emite.androidtv_project.presentation.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.emite.androidtv_project.domain.model.MediaType
import net.emite.androidtv_project.presentation.components.VideoPlayer
import net.emite.androidtv_project.presentation.theme.DarkBackground
import net.emite.androidtv_project.presentation.viewmodel.SlideshowUiState
import net.emite.androidtv_project.presentation.viewmodel.SlideshowViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SlideshowScreen(
    viewModel: SlideshowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentItem by viewModel.currentItem.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var logoutJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is SlideshowUiState.Success) {
            val orientation = state.config.orientation
            val activity = context.findActivity()
            if (activity != null) {
                Log.d("SlideshowScreen", "Cambiando orientación de pantalla a: $orientation")
                activity.requestedOrientation = if (orientation == "V") {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .onKeyEvent { event ->
                if (event.key == Key.Back || event.key == Key.Escape) {
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
                Text("Cargando configuración...")
            }

            is SlideshowUiState.Preloading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E1E1E)), // Fondo oscuro elegante (no negro puro)
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Branding / Publicidad
                        Text(
                            text = "TEGESTIONA",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = "Android TV Digital Signage",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.padding(40.dp))
                        
                        // Estado de descarga
                        Text(
                            text = "Sincronizando contenidos...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        LinearProgressIndicator(
                            progress = { state.current.toFloat() / state.total.toFloat() },
                            modifier = Modifier.width(300.dp),
                            color = Color.White,
                            trackColor = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "${state.current} / ${state.total}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            is SlideshowUiState.Error -> {
                Text(
                    text = "Error:\n\n${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }

            is SlideshowUiState.Success -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentItem,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(800)) togetherWith fadeOut(
                                animationSpec = tween(800)
                            )
                        },
                        label = "SlideshowTransition",
                        modifier = Modifier.fillMaxSize()
                    ) { item ->
                        item?.let {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                when (it.type) {
                                    MediaType.IMAGE -> {
                                        AsyncImage(
                                            model = viewModel.getLocalUri(it),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    MediaType.VIDEO -> {
                                        VideoPlayer(
                                            mediaUrl = viewModel.getLocalUri(it),
                                            modifier = Modifier.fillMaxSize(),
                                            onVideoEnded = viewModel::onMediaVideoEnded
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Aviso de red no bloqueante
                    state.networkWarning?.let { warning ->
                        NetworkWarningBadge(message = warning)
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkWarningBadge(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            color = Color(0xCC000000),   // negro semitransparente
            shape = RoundedCornerShape(8.dp)
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

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
