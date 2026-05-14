package net.emite.androidtv_project.presentation.slideshow.model

sealed class SlideMediaItem {
    abstract val uri: String
    abstract val intrinsicWidth: Int
    abstract val intrinsicHeight: Int

    data class Image(
        override val uri: String,
        override val intrinsicWidth: Int = 0,
        override val intrinsicHeight: Int = 0
    ) : SlideMediaItem()

    data class Video(
        override val uri: String,
        override val intrinsicWidth: Int = 0,
        override val intrinsicHeight: Int = 0
    ) : SlideMediaItem()
}
