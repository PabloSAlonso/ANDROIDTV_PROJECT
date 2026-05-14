package net.emite.androidtv_project.presentation.slideshow.components

import android.app.ActivityManager
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import net.emite.androidtv_project.presentation.slideshow.model.ScreenConfig
import net.emite.androidtv_project.presentation.slideshow.model.SlideMediaItem
import net.emite.androidtv_project.presentation.slideshow.util.SmartMediaScaler

@Composable
fun SmartSlideImage(
    item: SlideMediaItem.Image,
    screenConfig: ScreenConfig,
    modifier: Modifier = Modifier,
    usePreciseScaling: Boolean = false,
    onImageLoaded: ((Int, Int) -> Unit)? = null
) {
    var resolvedWidth by remember(item.uri) { mutableIntStateOf(item.intrinsicWidth) }
    var resolvedHeight by remember(item.uri) { mutableIntStateOf(item.intrinsicHeight) }

    val contentScale by remember(resolvedWidth, resolvedHeight, screenConfig, usePreciseScaling) {
        derivedStateOf {
            if (usePreciseScaling) {
                SmartMediaScaler.resolveContentScalePrecise(resolvedWidth, resolvedHeight, screenConfig)
            } else {
                SmartMediaScaler.resolveContentScale(resolvedWidth, resolvedHeight, screenConfig)
            }
        }
    }

    val imageRequest = rememberImageRequest(uri = item.uri)

    AsyncImage(
        model = imageRequest,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
        filterQuality = FilterQuality.High,
        onSuccess = { state ->
            val width = state.painter.intrinsicSize.width
            val height = state.painter.intrinsicSize.height
            if (width > 0 && height > 0 && width.isFinite() && height.isFinite()) {
                val intWidth = width.toInt()
                val intHeight = height.toInt()
                resolvedWidth = intWidth
                resolvedHeight = intHeight
                onImageLoaded?.invoke(intWidth, intHeight)
            }
        }
    )
}

@Composable
private fun rememberImageRequest(uri: String): ImageRequest {
    val context = LocalContext.current
    return remember(uri) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val isLowRam = memoryInfo.totalMem < 2L * 1024 * 1024 * 1024

        val builder = ImageRequest.Builder(context)
            .data(uri)
            .precision(Precision.EXACT)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            
        if (!isLowRam) {
            builder.size(Size.ORIGINAL)
        }
        
        builder.build()
    }
}
