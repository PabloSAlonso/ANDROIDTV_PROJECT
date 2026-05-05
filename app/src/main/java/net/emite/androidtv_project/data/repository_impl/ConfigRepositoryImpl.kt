package net.emite.androidtv_project.data.repository_impl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import net.emite.androidtv_project.data.local.dao.ConfigDao
import net.emite.androidtv_project.data.local.entity.ConfigEntity
import net.emite.androidtv_project.domain.model.Config
import net.emite.androidtv_project.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConfigRepositoryImpl @Inject constructor(
    private val configDao: ConfigDao,
    @ApplicationContext private val context: Context
) : ConfigRepository {
    
    private val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
    private val KEY_LAST_UPDATE = "last_update_timestamp"

    override fun getConfig(): Flow<Config?> {
        return configDao.getConfig().map { it?.toDomain() }
    }

    override suspend fun saveConfig(config: Config) {
        configDao.saveConfig(ConfigEntity.fromDomain(config))
    }

    override suspend fun clearConfig() {
        configDao.clearConfig()
        prefs.edit().remove(KEY_LAST_UPDATE).apply()
    }

    override suspend fun getLastUpdateTimestamp(): Long {
        return prefs.getLong(KEY_LAST_UPDATE, 0L)
    }

    override suspend fun saveLastUpdateTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_UPDATE, timestamp).apply()
    }
}
