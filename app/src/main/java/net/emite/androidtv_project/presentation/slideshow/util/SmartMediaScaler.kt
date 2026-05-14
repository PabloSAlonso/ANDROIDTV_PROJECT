package net.emite.androidtv_project.presentation.slideshow.util

import android.util.Log
import androidx.compose.ui.layout.ContentScale
import net.emite.androidtv_project.presentation.slideshow.model.ScreenConfig

object SmartMediaScaler {
    fun resolveContentScale(imageWidth: Int, imageHeight: Int, screenConfig: ScreenConfig): ContentScale {
        if (imageWidth == 0 || imageHeight == 0) return ContentScale.Fit
        
        val imageRatio = imageWidth.toFloat() / imageHeight.toFloat()
        
        val scale = if (!screenConfig.isVerticalMode) {
            if (imageRatio >= 1f) ContentScale.Crop else ContentScale.FillBounds
        } else {
            if (imageRatio < 1f) ContentScale.FillWidth else ContentScale.Fit
        }
        
        Log.d("SmartMediaScaler", "resolveContentScale: image=${imageWidth}x${imageHeight}, " +
                "vertical=${screenConfig.isVerticalMode} -> $scale")
        return scale
    }

    fun resolveContentScalePrecise(imageWidth: Int, imageHeight: Int, screenConfig: ScreenConfig): ContentScale {
        if (imageWidth == 0 || imageHeight == 0) return ContentScale.Fit
        
        val imageRatio = imageWidth.toFloat() / imageHeight.toFloat()
        val screenRatio = screenConfig.effectiveRatio
        val ratioDelta = imageRatio / screenRatio
        
        val scale = when {
            ratioDelta > 1.5f -> ContentScale.FillHeight
            ratioDelta > 0.85f -> ContentScale.Crop
            ratioDelta > 0.5f -> ContentScale.FillWidth
            else -> ContentScale.Fit
        }
        
        Log.d("SmartMediaScaler", "resolveContentScalePrecise: image=${imageWidth}x${imageHeight}, " +
                "ratioDelta=$ratioDelta -> $scale")
        return scale
    }
}
