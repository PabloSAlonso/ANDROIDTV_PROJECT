package net.emite.androidtv_project.presentation.slideshow.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import net.emite.androidtv_project.presentation.slideshow.model.ScreenConfig

/**
 * Cancels the asymmetric scale inherited from the global graphicsLayer in MainActivity,
 * restoring natural proportions for UI composables (text, icons, logos, overlays).
 *
 * Safe to call unconditionally: in horizontal mode (isVerticalMode = false) it returns
 * `this` unchanged with zero overhead.
 *
 * APPLY TO:   Text, Column/Row containing text, logos, icons, overlays, UI screens
 * DO NOT APPLY TO: SmartSlideImage, SmartSlideVideo, any media container —
 *              those composables intentionally occupy the deformed space.
 */
fun Modifier.undeformed(screenConfig: ScreenConfig): Modifier =
    if (!screenConfig.isVerticalMode) this
    else this.graphicsLayer {
        scaleX = screenConfig.counterScaleX
        scaleY = screenConfig.counterScaleY
    }
