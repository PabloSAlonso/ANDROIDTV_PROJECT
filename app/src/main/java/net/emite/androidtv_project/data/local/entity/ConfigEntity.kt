package net.emite.androidtv_project.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.emite.androidtv_project.domain.model.Config

@Entity(tableName = "config")
data class ConfigEntity(
    @PrimaryKey val id: Int = 0,
    val instancia: String,
    val orientation: String = "H",
    val isVertical: Boolean = false,
    val isInverted: Boolean = false
) {
    fun toDomain() = Config(
        id = id,
        instancia = instancia,
        orientation = orientation,
        isVertical = isVertical,
        isInverted = isInverted
    )

    companion object {
        fun fromDomain(config: Config) = ConfigEntity(
            id = config.id,
            instancia = config.instancia,
            orientation = config.orientation,
            isVertical = config.isVertical,
            isInverted = config.isInverted
        )
    }
}
