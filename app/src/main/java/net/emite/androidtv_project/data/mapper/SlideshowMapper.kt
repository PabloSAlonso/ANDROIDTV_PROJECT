package net.emite.androidtv_project.data.mapper

import net.emite.androidtv_project.data.remote.dto.ScreenDto
import net.emite.androidtv_project.domain.model.MediaType
import net.emite.androidtv_project.domain.model.SlideshowItem

private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "webm", "avi", "mov", "m4v", "ts", "mpeg")

fun ScreenDto.toDomainItem(instancia: String, folder: String): SlideshowItem {
    val mediaUrl = "https://$instancia.tegestiona.es/files/$folder/t_pantallas_media/${id}_$file"
    val extension = file.substringAfterLast('.', "").lowercase()
    val mediaType = if (extension in VIDEO_EXTENSIONS) MediaType.VIDEO else MediaType.IMAGE

    return SlideshowItem(
        id = id,
        mediaUrl = mediaUrl,
        durationSeconds = duracion?.toIntOrNull() ?: 10,
        type = mediaType
    )
}
