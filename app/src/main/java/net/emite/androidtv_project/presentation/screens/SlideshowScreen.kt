package net.emite.androidtv_project.presentation.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.tv.material3.*
import net.emite.androidtv_project.presentation.viewmodel.SlideshowUiState
import net.emite.androidtv_project.presentation.viewmodel.SlideshowViewModel

import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SlideshowScreen(
    viewModel: SlideshowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentItem by viewModel.currentItem.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var logoutJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is SlideshowUiState.Success) {
            val orientation = state.config.orientation
            val activity = context.findActivity()
            if (activity != null) {
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
            .onKeyEvent { event ->
                if (event.key == Key.Back || event.key == Key.Escape) {
                    when (event.type) {
                        KeyEventType.KeyDown -> {
                            if (logoutJob == null) {
                                logoutJob = scope.launch {
                                    delay(3000) // 3 segundos para logout
                                    viewModel.logout()
                                }
                            }
                            true
                        }
                        KeyEventType.KeyUp -> {
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
            .focusable(), // Importante para recibir eventos de teclado
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is SlideshowUiState.Loading -> {
                Text("Cargando Slideshow...")
            }
            is SlideshowUiState.Error -> {
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
            is SlideshowUiState.Success -> {
                currentItem?.let { item ->
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
