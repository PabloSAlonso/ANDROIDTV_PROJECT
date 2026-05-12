package net.emite.androidtv_project.presentation.components

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/**
 * Componente que envuelve un ExoPlayer para la reproducción de video en Jetpack Compose.
 * 
 * @param mediaUrl URL o ruta local del archivo de video.
 * @param modifier Modificadores para ajustar el diseño del reproductor.
 * @param onVideoEnded Función de callback invocada cuando el video llega a su fin.
 */
@androidx.compose.runtime.Composable
fun VideoPlayer(
    mediaUrl: String,
    modifier: Modifier = Modifier,
    onVideoEnded: () -> Unit
) {
    val context = LocalContext.current
    
    // Inicialización y configuración del reproductor ExoPlayer
    val exoPlayer = remember(mediaUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUrl))
            playWhenReady = true
            prepare()
        }
    }

    // Gestión del ciclo de vida del reproductor y sus listeners
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onVideoEnded()
                }
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Integración de la vista tradicional de Android (PlayerView) en Compose
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // Oculta los controles de reproducción
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // Escala para llenar la pantalla
            }
        },
        update = { playerView ->
            playerView.player = exoPlayer
        }
    )
}
