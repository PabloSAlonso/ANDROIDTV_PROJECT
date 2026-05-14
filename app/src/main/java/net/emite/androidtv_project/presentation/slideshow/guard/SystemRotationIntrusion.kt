package net.emite.androidtv_project.presentation.slideshow.guard

sealed class SystemRotationIntrusion {
    object None : SystemRotationIntrusion()
    object SensorOverrodeManualMode : SystemRotationIntrusion()
    object SensorForcedPortrait : SystemRotationIntrusion()
}
