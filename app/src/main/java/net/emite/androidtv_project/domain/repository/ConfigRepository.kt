package net.emite.androidtv_project.domain.repository

import net.emite.androidtv_project.domain.model.Config
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones para gestionar la configuración del dispositivo.
 */
interface ConfigRepository {
    /**
     * Obtiene la configuración actual del dispositivo como un flujo de datos reactivo.
     * @return Flow que emite el objeto [Config] actual o null si no existe.
     */
    fun getConfig(): Flow<Config?>

    /**
     * Guarda o actualiza la configuración del dispositivo.
     * @param config Objeto de configuración a persistir.
     */
    suspend fun saveConfig(config: Config)

    /**
     * Elimina la configuración almacenada.
     */
    suspend fun clearConfig()
}
