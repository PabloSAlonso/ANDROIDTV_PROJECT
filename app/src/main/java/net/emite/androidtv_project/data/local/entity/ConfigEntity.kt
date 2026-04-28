package net.emite.androidtv_project.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.emite.androidtv_project.domain.model.Config

@Entity(tableName = "config")
data class ConfigEntity(
    @PrimaryKey val id: Int = 0,
    val instancia: String
) {
    fun toDomain() = Config(
        id = id,
        instancia = instancia
    )

    companion object {
        fun fromDomain(config: Config) = ConfigEntity(
            id = config.id,
            instancia = config.instancia
        )
    }
}
