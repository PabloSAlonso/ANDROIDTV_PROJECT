package net.emite.androidtv_project.domain.model

data class SlideshowItem(
    val id: String,
    val imageUrl: String,
    val durationSeconds: Int
)

data class SlideshowConfig(
    val orientation: String,
    val items: List<SlideshowItem>
)
