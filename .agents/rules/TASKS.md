---
trigger: model_decision
description: Tareas del proyecto en android tv
---

We have an Android TV Jetpack Compose slideshow app with a manual vertical mode that rotates 
the entire UI 90° using `graphicsLayer(rotationZ = 90f)` in MainActivity. The app already has:

- `ScreenConfig` data class with `isVerticalMode`, `effectiveWidth/Height/Ratio`
- `SlideshowViewModel` with `isVerticalMode: StateFlow<Boolean>` as single source of truth
- `rememberScreenConfig()` composable that uses `remember(isVerticalMode)` (ignores Configuration)
- `requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE` set in onCreate and manifest

## Problem

Some Android TV devices (Sony Bravia, TCL with Google TV, Fire TV with motion sensor) have 
orientation sensors that can fire and override our manual rotation. When the sensor acts on 
its own, three things can break:

1. The system rotates the Activity independently of our graphicsLayer rotation, resulting in 
   a double rotation (180° total) or a cancelled rotation (0° when we want 90°)
2. `Configuration.orientation` changes to PORTRAIT even though we locked landscape, because 
   some custom TV ROMs ignore `requestedOrientation` for sensor events
3. There is no way to know it happened unless we explicitly detect the discrepancy between 
   what we ordered (ViewModel) and what the system reports (Configuration)

## Solution to implement

### 1. `SystemRotationIntrusion` sealed class
Create in `ui/slideshow/guard/SystemRotationIntrusion.kt`:
- `object None`
- `object SensorOverrodeManualMode`  ← sensor fired while our vertical mode was active
- `object SensorForcedPortrait`      ← sensor fired while we were in normal landscape mode

### 2. `SystemRotationGuard` class
Create in `ui/slideshow/guard/SystemRotationGuard.kt`:

Constructor receives `activity: ComponentActivity` and `viewModel: SlideshowViewModel`.

Internal state:
- `ourIntent: StateFlow<Boolean>` = `viewModel.isVerticalMode`
- `systemOrientation: MutableStateFlow<Int>` initialized from 
  `activity.resources.configuration.orientation`
- `lastConfig: Configuration?` to detect if ONLY orientation changed (sensor) vs other 
  fields changed too (HDMI hotplug, DPI change — NOT a sensor intrusion)

Public `intrusionDetected: StateFlow<SystemRotationIntrusion>` built with:
```kotlin
combine(ourIntent, systemOrientation) { weWantVertical, sysOrientation ->
    val systemThinkPortrait = sysOrientation == Configuration.ORIENTATION_PORTRAIT
    when {
        weWantVertical && systemThinkPortrait  -> SystemRotationIntrusion.SensorOverrodeManualMode
        !weWantVertical && systemThinkPortrait -> SystemRotationIntrusion.SensorForcedPortrait
        else                                  -> SystemRotationIntrusion.None
    }
}
.stateIn(activity.lifecycleScope, SharingStarted.WhileSubscribed(5_000), 
         SystemRotationIntrusion.None)
```

Public `onConfigurationChanged(newConfig: Configuration)` method:
- Compare `newConfig.orientation` vs current `systemOrientation.value`
- Only update `systemOrientation` and treat as potential intrusion if ONLY orientation changed:
  `orientationChanged && newConfig.screenWidthDp == lastConfig?.screenWidthDp 
   && newConfig.densityDpi == lastConfig?.densityDpi`
- If other fields changed too, it is a legitimate system event (HDMI hotplug, MultiWindow),
  update `lastConfig` but do NOT emit as intrusion
- Always update `lastConfig = newConfig`

### 3. Extend `SlideshowViewModel`
Add to the existing ViewModel:
```kotlin
private val _sensorIntrusionCount = MutableStateFlow(0)
val sensorIntrusionCount: StateFlow<Int> = _sensorIntrusionCount.asStateFlow()

private val _lastIntrusion = MutableStateFlow<SystemRotationIntrusion>(
    SystemRotationIntrusion.None
)
val lastIntrusion: StateFlow<SystemRotationIntrusion> = _lastIntrusion.asStateFlow()

fun reportSensorIntrusion(intrusion: SystemRotationIntrusion) {
    _sensorIntrusionCount.update { it + 1 }
    _lastIntrusion.value = intrusion
}
```

Do NOT add any sensor reading logic to the ViewModel. It must remain unaware of Android APIs.
The ViewModel only receives reports; the detection lives in SystemRotationGuard.

### 4. Modify `MainActivity`

Add these changes to the existing MainActivity (do not rewrite unrelated code):

a) Instantiate guard after super.onCreate:
```kotlin
private lateinit var rotationGuard: SystemRotationGuard
// in onCreate, after requestedOrientation:
rotationGuard = SystemRotationGuard(this, viewModel)
```

b) Collect intrusion events in a coroutine launched in onCreate (after setContent is fine):
```kotlin
lifecycleScope.launch {
    rotationGuard.intrusionDetected
        .distinctUntilChanged()
        .collect { intrusion ->
            when (intrusion) {
                is SystemRotationIntrusion.None -> Unit
                else -> {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    viewModel.reportSensorIntrusion(intrusion)
                }
            }
        }
}
```

c) Override `onConfigurationChanged` and forward to guard:
```kotlin
override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    rotationGuard.onConfigurationChanged(newConfig)
}
```

d) Verify `AndroidManifest.xml` already has (add if missing):
```xml
android:screenOrientation="landscape"
android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
```

### 5. File structure
ui/
slideshow/
guard/
SystemRotationIntrusion.kt   ← new
SystemRotationGuard.kt       ← new
SlideshowViewModel.kt          ← extend (add intrusion tracking fields)
MainActivity.kt                    ← extend (add guard instantiation + collection)
AndroidManifest.xml                ← verify/add configChanges attribute

## Contracts to preserve

- `SlideshowViewModel.isVerticalMode` remains the single source of truth for rotation intent.
  Never update it from SystemRotationGuard or from onConfigurationChanged.
- `rememberScreenConfig()` must keep `remember(isVerticalMode)` as its only key.
  Do not add `configuration` back as a key — that would undo the protection already in place.
- SystemRotationGuard must NOT hold a reference to any Composable or Context beyond 
  Activity lifecycle. Use `activity.lifecycleScope` for coroutines, not `GlobalScope`.
- The correction (`requestedOrientation = LANDSCAPE`) must happen in the collect lambda 
  inside MainActivity, not inside SystemRotationGuard. The guard only detects and reports.

## Do NOT

- Do not read `OrientationEventListener` inside Composables
- Do not add `configuration` as a key to `rememberScreenConfig()`
- Do not create a new `SystemRotationGuard` on every recomposition
- Do not call `viewModel.setVerticalMode()` from the guard — the guard never changes intent,
  only reports that the system tried to override it
- Do not use `GlobalScope` for the intrusion collection coroutine

## Verify after implementation

1. With `isVerticalMode = true`: rotate the device physically (or use ADB 
   `adb shell settings put system accelerometer_rotation 1` then tilt) → 
   `intrusionDetected` emits `SensorOverrodeManualMode` and `requestedOrientation` 
   is immediately reset to LANDSCAPE
2. With `isVerticalMode = false`: same physical rotation → emits `SensorForcedPortrait` 
   and resets to LANDSCAPE
3. Simulate HDMI hotplug via ADB (`adb shell wm size 1920x1080` then reset) → 
   guard does NOT emit intrusion (screenWidthDp changed, so it is filtered out)
4. `sensorIntrusionCount` increments on each intrusion, never on legitimate config changes
5. No recomposition triggered by the guard itself — all corrections happen at Activity level