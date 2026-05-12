package net.emite.androidtv_project.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.emite.androidtv_project.domain.model.Config

/**
 * Entidad de base de datos para almacenar la configuración del dispositivo.
 *
 * @property id Identificador único de la configuración.
 * @property instancia Nombre de la instancia del dispositivo (ej. "emite").
 * @property orientation Código de orientación ("H", "V", "VI").
 * @property isVertical Flag que indica si el modo es vertical.
 * @property isInverted Flag que indica si la orientación está invertida.
 */
@Entity(tableName = "config")
data class ConfigEntity(
    @PrimaryKey val id: Int = 0,
    val instancia: String,
    val orientation: String = "H",
    val isVertical: Boolean = false,
    val isInverted: Boolean = false
) {
    /**
     * Convierte esta entidad de base de datos al modelo de dominio [Config].
     */
    fun toDomain() = Config(
        id = id,
        instancia = instancia,
        orientation = orientation,
        isVertical = isVertical,
        isInverted = isInverted
    )

    companion object {
        /**
         * Crea una entidad de base de datos a partir de un modelo de dominio [Config].
         */
        fun fromDomain(config: Config) = ConfigEntity(
            id = config.id,
            instancia = config.instancia,
            orientation = config.orientation,
            isVertical = config.isVertical,
            isInverted = config.isInverted
        )
    }
}
