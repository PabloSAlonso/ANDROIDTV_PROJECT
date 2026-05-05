package net.emite.androidtv_project.domain.repository

import net.emite.androidtv_project.domain.model.Config
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    fun getConfig(): Flow<Config?>
    suspend fun saveConfig(config: Config)
    suspend fun clearConfig()
    suspend fun getLastUpdateTimestamp(): Long
    suspend fun saveLastUpdateTimestamp(timestamp: Long)
}
