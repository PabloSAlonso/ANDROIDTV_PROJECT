package net.emite.androidtv_project.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.emite.androidtv_project.data.local.entity.ConfigEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para gestionar la configuración persistente del dispositivo.
 */
@Dao
interface ConfigDao {
    /**
     * Obtiene la configuración del dispositivo como un flujo de datos reactivo.
     * @return [Flow] que emite la [ConfigEntity] actual o null.
     */
    @Query("SELECT * FROM config WHERE id = 0")
    fun getConfig(): Flow<ConfigEntity?>

    /**
     * Guarda o actualiza la configuración del dispositivo.
     * @param config Entidad de configuración a persistir.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ConfigEntity)

    /**
     * Elimina todos los registros de configuración.
     */
    @Query("DELETE FROM config")
    suspend fun clearConfig()
}
