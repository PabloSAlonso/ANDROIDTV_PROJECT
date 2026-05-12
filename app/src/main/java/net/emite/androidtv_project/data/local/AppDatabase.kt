package net.emite.androidtv_project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.emite.androidtv_project.data.local.dao.CachedJsonDao
import net.emite.androidtv_project.data.local.dao.ConfigDao
import net.emite.androidtv_project.data.local.entity.CachedJsonEntity
import net.emite.androidtv_project.data.local.entity.ConfigEntity

/**
 * Base de datos principal de la aplicación utilizando Room.
 * Almacena la configuración del dispositivo y la caché del JSON del slideshow.
 */
@Database(entities = [ConfigEntity::class, CachedJsonEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Provee acceso a las operaciones de la tabla de configuración.
     */
    abstract fun configDao(): ConfigDao

    /**
     * Provee acceso a las operaciones de la tabla de caché de JSON.
     */
    abstract fun cachedJsonDao(): CachedJsonDao

    companion object {
        /**
         * Migración de la versión 2 a la 3 para incluir la tabla de caché de JSON.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `cached_json` " +
                    "(`id` INTEGER NOT NULL, `rawJson` TEXT NOT NULL, " +
                    "`lastSavedTimestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }
    }
}
