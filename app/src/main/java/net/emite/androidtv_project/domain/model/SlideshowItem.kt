package net.emite.androidtv_project.domain.model

/**
 * Define los tipos de medios soportados en el slideshow.
 */
enum class MediaType {
    /** Recurso de imagen estática. */
    IMAGE,
    /** Recurso de video. */
    VIDEO
}

/**
 * Representa un elemento individual dentro de un carrusel de diapositivas.
 *
 * @property id Identificador único del elemento.
 * @property mediaUrl URL remota del recurso multimedia.
 * @property durationSeconds Tiempo de exposición en segundos.
 * @property type Tipo de medio (Imagen o Video).
 * @property orden Posición secuencial en el carrusel.
 * @property semana Días de la semana programados para su visualización.
 * @property horas Franjas horarias programadas para su visualización.
 * @property md5 Hash de verificación de integridad del archivo.
 */
data class SlideshowItem(
    val id: String,
    val mediaUrl: String,
    val durationSeconds: Int,
    val type: MediaType,
    val orden: Int,
    val semana: String?,
    val horas: String?,
    val md5: String?
)

/**
 * Configuración completa de un carrusel de diapositivas.
 *
 * @property orientation Orientación deseada para el carrusel ("H" o "V").
 * @property items Lista de elementos que componen el carrusel.
 */
data class SlideshowConfig(
    val orientation: String,
    val items: List<SlideshowItem>
)
