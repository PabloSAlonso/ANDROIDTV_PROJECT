package net.emite.androidtv_project.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.emite.androidtv_project.data.local.entity.CachedJsonEntity

/**
 * Data Access Object (DAO) para gestionar la caché del JSON de configuración.
 */
@Dao
interface CachedJsonDao {
    /**
     * Recupera el JSON de configuración almacenado.
     * @return [CachedJsonEntity] si existe, null en caso contrario.
     */
    @Query("SELECT * FROM cached_json WHERE id = 1")
    suspend fun getCachedJson(): CachedJsonEntity?

    /**
     * Inserta o actualiza el JSON de configuración en la caché.
     * @param entity Entidad a persistir.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedJson(entity: CachedJsonEntity)

    /**
     * Elimina todos los datos de la caché del JSON.
     */
    @Query("DELETE FROM cached_json")
    suspend fun clearCachedJson()
}
