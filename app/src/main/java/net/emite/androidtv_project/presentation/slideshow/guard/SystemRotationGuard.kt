package net.emite.androidtv_project.presentation.slideshow.guard

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import net.emite.androidtv_project.presentation.viewmodel.SlideshowViewModel

class SystemRotationGuard(
    activity: ComponentActivity,
    viewModel: SlideshowViewModel
) {
    private val ourIntent: StateFlow<Boolean> = viewModel.isVerticalMode
    private val systemOrientation = MutableStateFlow(activity.resources.configuration.orientation)
    private var lastConfig: Configuration? = Configuration(activity.resources.configuration)

    val intrusionDetected: StateFlow<SystemRotationIntrusion> = combine(
        ourIntent, systemOrientation
    ) { weWantVertical, sysOrientation ->
        val systemThinkPortrait = sysOrientation == Configuration.ORIENTATION_PORTRAIT
        when {
            weWantVertical && systemThinkPortrait -> SystemRotationIntrusion.SensorOverrodeManualMode
            !weWantVertical && systemThinkPortrait -> SystemRotationIntrusion.SensorForcedPortrait
            else -> SystemRotationIntrusion.None
        }
    }.stateIn(
        scope = activity.lifecycleScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SystemRotationIntrusion.None
    )

    fun onConfigurationChanged(newConfig: Configuration) {
        val orientationChanged = newConfig.orientation != systemOrientation.value
        
        // Only consider it an intrusion if ONLY orientation changed (likely a sensor event)
        val isSensorEvent = orientationChanged && 
            newConfig.screenWidthDp == lastConfig?.screenWidthDp && 
            newConfig.densityDpi == lastConfig?.densityDpi
            
        if (isSensorEvent) {
            systemOrientation.value = newConfig.orientation
        }
        
        lastConfig = Configuration(newConfig)
    }
}
