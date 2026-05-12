package net.emite.androidtv_project.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Respuesta raíz de la API para la configuración del slideshow.
 *
 * @property cfg Información general de configuración (orientación, etc.).
 * @property screens Mapa de pantallas identificadas por una clave, conteniendo la información de los medios.
 */
@Serializable
data class SlideshowResponse(
    val cfg: ConfigDto,
    val screens: Map<String, ScreenDto>
)

/**
 * DTO que representa la configuración general enviada por la API.
 *
 * @property orientacion Orientación de la pantalla ("H" o "V").
 * @property url URL base opcional.
 */
@Serializable
data class ConfigDto(
    val orientacion: String? = "H",
    val url: String? = null
)

/**
 * DTO que representa un elemento multimedia (pantalla) enviado por la API.
 *
 * @property id Identificador del medio.
 * @property file Nombre del archivo o URL del medio.
 * @property duracion Tiempo de exposición en segundos (como String desde la API).
 * @property orden Posición en la secuencia (como String desde la API).
 * @property semana Días de la semana permitidos.
 * @property horas Horas permitidas.
 * @property md5 Hash para validación de integridad.
 */
@Serializable
data class ScreenDto(
    val id: String,
    val file: String,
    val duracion: String? = "10",
    val orden: String? = "0",
    val semana: String? = null,
    val horas: String? = null,
    val md5: String? = null
)
