---
trigger: model_decision
description: Tareas del proyecto en android tv
---

Implement a smart image/video scaling system for an Android TV Jetpack Compose slideshow app 
that supports a vertical signage mode (global 90° rotation via graphicsLayer in MainActivity).

## Context

The app displays image and video slideshows on Android TV. A "vertical mode" rotates the entire 
UI 90° using `graphicsLayer(rotationZ = 90f)` and manual scaleX/scaleY applied globally from 
MainActivity. The key problem: Compose's logical viewport does NOT update when graphicsLayer 
rotates it, so a 1920×1080 screen rotated 90° still reports 1920×1080 as its dimensions instead 
of 1080×1920. This causes incorrect ContentScale decisions for portrait images.

## Architecture to implement

### 1. Data models — create in `ui/slideshow/model/`

`SlideMediaItem` sealed class:
- `Image(uri: Uri, intrinsicWidth: Int = 0, intrinsicHeight: Int = 0)`
- `Video(uri: Uri, intrinsicWidth: Int = 0, intrinsicHeight: Int = 0)`

`ScreenConfig` data class:
- `isVerticalMode: Boolean`
- `viewportWidth: Int`, `viewportHeight: Int`
- Computed properties: `effectiveWidth`, `effectiveHeight`, `effectiveRatio`
  (swap width/height when isVerticalMode = true)

### 2. SmartMediaScaler — create in `ui/slideshow/util/SmartMediaScaler.kt`

Singleton object with two methods:

`resolveContentScale(imageWidth, imageHeight, screenConfig): ContentScale`
Logic:
- If NOT vertical mode:
  - imageRatio >= 1f → ContentScale.Crop
  - imageRatio < 1f  → ContentScale.FillBounds
- If vertical mode:
  - imageRatio < 1f  → ContentScale.FillWidth
  - imageRatio >= 1f → ContentScale.Fit

`resolveContentScalePrecise(imageWidth, imageHeight, screenConfig): ContentScale`
Logic based on ratioDelta = imageRatio / screenRatio:
- ratioDelta > 1.5f  → ContentScale.FillHeight
- ratioDelta > 0.85f → ContentScale.Crop
- ratioDelta > 0.5f  → ContentScale.FillWidth
- else               → ContentScale.Fit

### 3. SmartSlideImage composable — create in `ui/slideshow/components/SmartSlideImage.kt`

- Parameters: `item: SlideMediaItem.Image`, `screenConfig: ScreenConfig`, 
  `modifier: Modifier`, `usePreciseScaling: Boolean = false`,
  `onImageLoaded: ((Int, Int) -> Unit)? = null`
- Uses `var resolvedWidth/Height` state initialized from `item.intrinsicWidth/Height`
- Uses `derivedStateOf` to compute `contentScale` reactively from resolved dimensions + screenConfig
- Calls the appropriate `SmartMediaScaler` method based on `usePreciseScaling`
- Uses Coil `AsyncImage` with:
  - `Precision.EXACT`
  - `CachePolicy.ENABLED` for both disk and memory
  - `crossfade(true)`
  - `Size.ORIGINAL` (unless device RAM < 2GB, then use target size)
  - `filterQuality = FilterQuality.High`
  - `onSuccess` callback that reads `painter.intrinsicSize` and updates `resolvedWidth/Height`

Helper `rememberImageRequest(uri)` private composable using `remember(uri)`.

### 4. SmartSlideVideo composable — create in `ui/slideshow/components/SmartSlideVideo.kt`

- Parameters: `item: SlideMediaItem.Video`, `screenConfig: ScreenConfig`, `modifier: Modifier`
- Uses ExoPlayer with `repeatMode = REPEAT_MODE_ONE`, `volume = 0f`
- `LaunchedEffect(item.uri)` sets media item, prepares, and plays
- `DisposableEffect` releases player on dispose
- Computes `resizeMode` (AspectRatioFrameLayout constants) using the same logic as SmartMediaScaler:
  - Not vertical + horizontal video → RESIZE_MODE_ZOOM
  - Vertical mode + portrait video  → RESIZE_MODE_FILL
  - Otherwise                       → RESIZE_MODE_FIT
- Renders via `AndroidView` with `PlayerView(useController = false)`

### 5. SlideshowContainer composable — create in `ui/slideshow/SlideshowContainer.kt`

- Parameters: `currentItem: SlideMediaItem`, `screenConfig: ScreenConfig`, `modifier: Modifier`
- Uses `Crossfade(targetState = currentItem, animationSpec = tween(800, easing = LinearEasing))`
- Delegates to `SmartSlideImage` or `SmartSlideVideo` based on sealed class type

### 6. MainActivity integration

- Read `isVerticalMode` from a `SlideshowViewModel` StateFlow (use a simple `MutableStateFlow` 
  for now with a hardcoded initial value of `false`)
- Build `ScreenConfig` using `LocalConfiguration.current` inside a `remember(configuration, 
  isVerticalMode)` block
- Apply `graphicsLayer { if (isVerticalMode) { rotationZ = 90f; val s = size.width/size.height; 
  scaleX = s; scaleY = s } }` on the root `Box`
- Pass `screenConfig` down to `SlideshowContainer`
- Do NOT read `LocalConfiguration` directly inside media composables — always use the passed 
  `ScreenConfig`

## Technical requirements

- Language: Kotlin with Jetpack Compose
- Coil version: 2.x (`io.coil-kt:coil-compose`)
- ExoPlayer: `androidx.media3:media3-exoplayer` + `media3-ui`
- Minimum API: 21, target: Android TV (API 28+)
- All composables must be `@Composable` functions in their own files
- Use `remember`, `derivedStateOf`, `LaunchedEffect`, `DisposableEffect` appropriately
- No business logic inside composables — SmartMediaScaler must be a pure object/utility

## File structure to create

ui/
slideshow/
model/
SlideMediaItem.kt
ScreenConfig.kt
util/
SmartMediaScaler.kt
components/
SmartSlideImage.kt
SmartSlideVideo.kt
SlideshowContainer.kt
MainActivity.kt (modify existing)

## Do NOT

- Do not use `ContentScale.Crop` unconditionally
- Do not read screen dimensions directly inside `SmartSlideImage` or `SmartSlideVideo`
- Do not use `LocalConfiguration.current` inside media composables
- Do not create a new ExoPlayer instance on every recomposition
- Do not use `Size.ORIGINAL` in Coil if you can determine the target composable size

## Verify after implementation

1. A 1080×1920 image in vertical mode fills the screen without excessive zoom
2. A 1920×1080 image in vertical mode shows Fit scaling (small letterbox acceptable)
3. A 1920×1080 image in horizontal mode uses Crop (no black bars)
4. Switching `isVerticalMode` in the ViewModel triggers recomposition and rescaling
5. ExoPlayer is released when the composable leaves composition