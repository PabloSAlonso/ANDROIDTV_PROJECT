package net.emite.androidtv_project.presentation.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
                Text("Cargando Slideshow...")
            }

            is SlideshowUiState.Error -> {
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }

            is SlideshowUiState.Success -> {
                currentItem?.let { item ->
                    when (item.type) {
                        MediaType.IMAGE -> {
                            AsyncImage(
                                model = item.mediaUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        MediaType.VIDEO -> {
                            VideoPlayer(
                                mediaUrl = item.mediaUrl,
                                modifier = Modifier.fillMaxSize(),
                                onVideoEnded = viewModel::onMediaVideoEnded
                            )
                        }
                    }
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
