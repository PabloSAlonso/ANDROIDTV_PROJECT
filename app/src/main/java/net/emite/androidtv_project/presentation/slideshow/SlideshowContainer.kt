package net.emite.androidtv_project.presentation.slideshow

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.emite.androidtv_project.presentation.slideshow.components.SmartSlideImage
import net.emite.androidtv_project.presentation.slideshow.components.SmartSlideVideo
import net.emite.androidtv_project.presentation.slideshow.model.ScreenConfig
import net.emite.androidtv_project.presentation.slideshow.model.SlideMediaItem

@Composable
fun SlideshowContainer(
    currentItem: SlideMediaItem?,
    screenConfig: ScreenConfig,
    modifier: Modifier = Modifier
) {
    if (currentItem == null) return

    Crossfade(
        targetState = currentItem,
        animationSpec = tween(800, easing = LinearEasing),
        label = "SlideshowTransition",
        modifier = modifier
    ) { item ->
        when (item) {
            is SlideMediaItem.Image -> {
                SmartSlideImage(
                    item = item,
                    screenConfig = screenConfig,
                    modifier = Modifier.fillMaxSize(),
                    usePreciseScaling = false
                )
            }
            is SlideMediaItem.Video -> {
                SmartSlideVideo(
                    item = item,
                    screenConfig = screenConfig,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
