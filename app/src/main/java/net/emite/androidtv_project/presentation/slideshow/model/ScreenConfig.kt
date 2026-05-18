package net.emite.androidtv_project.presentation.slideshow.model

data class ScreenConfig(
    val isVerticalMode: Boolean,
    val viewportWidth: Int,
    val viewportHeight: Int
) {
    val effectiveWidth: Int
        get() = if (isVerticalMode) viewportHeight else viewportWidth
        
    val effectiveHeight: Int
        get() = if (isVerticalMode) viewportWidth else viewportHeight
        
    val effectiveRatio: Float
        get() = if (effectiveHeight > 0) effectiveWidth.toFloat() / effectiveHeight.toFloat() else 1f

    // New: mirrors exactly what MainActivity applies in graphicsLayer
    val globalScaleX: Float
        get() = if (isVerticalMode) viewportHeight.toFloat() / viewportWidth.toFloat() else 1f
    val globalScaleY: Float
        get() = if (isVerticalMode) viewportWidth.toFloat() / viewportHeight.toFloat() else 1f

    // New: the exact inverse — cancels the distortion for UI composables
    val counterScaleX: Float get() = if (globalScaleX != 0f) 1f / globalScaleX else 1f
    val counterScaleY: Float get() = if (globalScaleY != 0f) 1f / globalScaleY else 1f
}
