package net.emite.androidtv_project.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.emite.androidtv_project.data.local.entity.CachedJsonEntity

@Dao
interface CachedJsonDao {
    @Query("SELECT * FROM cached_json WHERE id = 1")
    suspend fun getCachedJson(): CachedJsonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedJson(entity: CachedJsonEntity)

    @Query("DELETE FROM cached_json")
    suspend fun clearCachedJson()
}
