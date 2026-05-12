package net.emite.androidtv_project.data.repository_impl

import net.emite.androidtv_project.data.local.dao.ConfigDao
import net.emite.androidtv_project.data.local.entity.ConfigEntity
import net.emite.androidtv_project.domain.model.Config
import net.emite.androidtv_project.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementación de [ConfigRepository] que utiliza Room para la persistencia de datos.
 *
 * @property configDao DAO para acceder a la tabla de configuración en la base de datos local.
 */
class ConfigRepositoryImpl @Inject constructor(
    private val configDao: ConfigDao
) : ConfigRepository {
    
    /**
     * Obtiene la configuración desde la base de datos local y la mapea al modelo de dominio.
     * @return Flow que emite la configuración actual o null.
     */
    override fun getConfig(): Flow<Config?> {
        return configDao.getConfig().map { it?.toDomain() }
    }

    /**
     * Persiste una nueva configuración o actualiza la existente en la base de datos.
     * @param config Objeto de dominio con los datos a guardar.
     */
    override suspend fun saveConfig(config: Config) {
        configDao.saveConfig(ConfigEntity.fromDomain(config))
    }

    /**
     * Elimina todos los datos de configuración de la base de datos local.
     */
    override suspend fun clearConfig() {
        configDao.clearConfig()
    }
}
