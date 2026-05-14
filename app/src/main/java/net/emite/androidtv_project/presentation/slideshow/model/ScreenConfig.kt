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
}
