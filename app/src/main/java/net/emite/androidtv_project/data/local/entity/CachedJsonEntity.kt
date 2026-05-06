package net.emite.androidtv_project.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_json")
data class CachedJsonEntity(
    @PrimaryKey val id: Int = 1,           // Siempre fila única
    val rawJson: String,                    // JSON completo como String
    val lastSavedTimestamp: Long            // Cuándo se guardó
)
