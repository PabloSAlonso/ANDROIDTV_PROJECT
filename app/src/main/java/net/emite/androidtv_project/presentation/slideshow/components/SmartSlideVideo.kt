package net.emite.androidtv_project.presentation.slideshow.components

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import net.emite.androidtv_project.presentation.slideshow.model.ScreenConfig
import net.emite.androidtv_project.presentation.slideshow.model.SlideMediaItem

@OptIn(UnstableApi::class)
@Composable
fun SmartSlideVideo(
    item: SlideMediaItem.Video,
    screenConfig: ScreenConfig,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            
            addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    android.util.Log.e("SmartSlideVideo", "Player error: ${error.message}", error)
                }

                override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                    android.util.Log.d("SmartSlideVideo", "Video size changed: width=${videoSize.width}, height=${videoSize.height}, unappliedRotationDegrees=${videoSize.unappliedRotationDegrees}, pixelWidthHeightRatio=${videoSize.pixelWidthHeightRatio}")
                }
                
                override fun onRenderedFirstFrame() {
                    android.util.Log.d("SmartSlideVideo", "Rendered first frame")
                }
                
                override fun onPlaybackStateChanged(playbackState: Int) {
                    val stateStr = when(playbackState) {
                        Player.STATE_IDLE -> "STATE_IDLE"
                        Player.STATE_BUFFERING -> "STATE_BUFFERING"
                        Player.STATE_READY -> "STATE_READY"
                        Player.STATE_ENDED -> "STATE_ENDED"
                        else -> "UNKNOWN"
                    }
                    android.util.Log.d("SmartSlideVideo", "Playback state changed: $stateStr")
                }
            })
        }
    }
    
    val isHorizontalVideo = if (item.intrinsicWidth > 0 && item.intrinsicHeight > 0) {
        item.intrinsicWidth >= item.intrinsicHeight
    } else {
        true // Assume horizontal if unknown, common for TVs
    }
    
    val resizeMode = remember(screenConfig.isVerticalMode, isHorizontalVideo) {
        if (!screenConfig.isVerticalMode && isHorizontalVideo) {
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else if (screenConfig.isVerticalMode && !isHorizontalVideo) {
            AspectRatioFrameLayout.RESIZE_MODE_FILL
        } else {
            AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

    LaunchedEffect(item.uri) {
        val mediaItem = MediaItem.fromUri(item.uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            val view = android.view.LayoutInflater.from(ctx).inflate(net.emite.androidtv_project.R.layout.view_video_player, null) as PlayerView
            view.apply {
                player = exoPlayer
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
                    android.util.Log.d("SmartSlideVideo", "PlayerView layout: width=${right - left}, height=${bottom - top}")
                }
            }
        },
        update = { playerView ->
            playerView.resizeMode = resizeMode
        },
        modifier = modifier
    )
}
