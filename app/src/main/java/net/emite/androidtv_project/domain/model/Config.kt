package net.emite.androidtv_project.domain.model

/**
 * Representa la configuración local del dispositivo.
 *
 * @property id Identificador único de la configuración en la base de datos local.
 * @property instancia Identificador de la instancia o dispositivo asignado.
 * @property orientation Código de orientación de la pantalla ("H" para horizontal, "V" para vertical, etc.).
 * @property isVertical Indica si la pantalla está en modo vertical.
 * @property isInverted Indica si la orientación vertical está invertida.
 */
data class Config(
    val id: Int = 0,
    val instancia: String,
    val orientation: String = "H",
    val isVertical: Boolean = false,
    val isInverted: Boolean = false
)
