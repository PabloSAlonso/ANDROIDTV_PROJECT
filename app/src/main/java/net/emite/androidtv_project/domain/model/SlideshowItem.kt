package net.emite.androidtv_project.domain.model

enum class MediaType {
    IMAGE,
    VIDEO
}

data class SlideshowItem(
    val id: String,
    val mediaUrl: String,
    val durationSeconds: Int,
    val type: MediaType,
    val orden: Int,
    val semana: String?,
    val horas: String?
)

data class SlideshowConfig(
    val orientation: String,
    val items: List<SlideshowItem>
)
