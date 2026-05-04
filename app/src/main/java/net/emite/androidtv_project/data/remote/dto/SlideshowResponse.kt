package net.emite.androidtv_project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SlideshowResponse(
    val cfg: ConfigDto,
    val screens: Map<String, ScreenDto>
)

@Serializable
data class ConfigDto(
    val orientacion: String? = "H",
    val url: String? = null
)

@Serializable
data class ScreenDto(
    val id: String,
    val file: String,
    val duracion: String? = "10",
    val orden: String? = "0",
    val semana: String? = null,
    val horas: String? = null
)
