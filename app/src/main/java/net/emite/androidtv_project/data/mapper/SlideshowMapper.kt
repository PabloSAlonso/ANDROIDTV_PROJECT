package net.emite.androidtv_project.data.mapper

import net.emite.androidtv_project.data.remote.dto.ScreenDto
import net.emite.androidtv_project.domain.model.MediaType
import net.emite.androidtv_project.domain.model.SlideshowItem

/**
 * Conjunto de extensiones de archivo que se consideran video.
 */
private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "webm", "avi", "mov", "m4v", "ts", "mpeg")

/**
 * Convierte un objeto de transferencia de datos [ScreenDto] en un modelo de dominio [SlideshowItem].
 * 
 * Realiza las siguientes tareas:
 * 1. Construye la URL completa del recurso multimedia basándose en la instancia y carpeta proporcionadas.
 * 2. Determina el tipo de medio ([MediaType]) analizando la extensión del archivo.
 * 3. Parsea duraciones y órdenes de String a Int.
 * 
 * @param instancia Nombre de la instancia del dispositivo para construir la URL.
 * @param folder Nombre de la carpeta de la instancia para construir la URL.
 * @return Un objeto [SlideshowItem] listo para ser usado por la lógica de negocio.
 */
fun ScreenDto.toDomainItem(instancia: String, folder: String): SlideshowItem {
    val mediaUrl = "https://$instancia.tegestiona.es/files/$folder/t_pantallas_media/${id}_$file"
    val extension = file.substringAfterLast('.', "").lowercase()
    val mediaType = if (extension in VIDEO_EXTENSIONS) MediaType.VIDEO else MediaType.IMAGE

    return SlideshowItem(
        id = id,
        mediaUrl = mediaUrl,
        durationSeconds = duracion?.toIntOrNull() ?: 10,
        type = mediaType,
        orden = orden?.toIntOrNull() ?: 0,
        semana = semana,
        horas = horas,
        md5 = md5
    )
}
