package net.emite.androidtv_project.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa la caché persistente del JSON de configuración del slideshow.
 * Se utiliza para almacenar la última respuesta exitosa de la API y permitir el funcionamiento offline.
 *
 * @property id Identificador de la fila (generalmente único, valor por defecto 1).
 * @property rawJson Contenido íntegro del JSON recibido de la API.
 * @property lastSavedTimestamp Marca de tiempo en milisegundos de la última actualización.
 */
@Entity(tableName = "cached_json")
data class CachedJsonEntity(
    @PrimaryKey val id: Int = 1,
    val rawJson: String,
    val lastSavedTimestamp: Long
)
