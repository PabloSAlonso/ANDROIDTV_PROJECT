package net.emite.androidtv_project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.emite.androidtv_project.data.local.dao.CachedJsonDao
import net.emite.androidtv_project.data.local.dao.ConfigDao
import net.emite.androidtv_project.data.local.entity.CachedJsonEntity
import net.emite.androidtv_project.data.local.entity.ConfigEntity

@Database(entities = [ConfigEntity::class, CachedJsonEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao
    abstract fun cachedJsonDao(): CachedJsonDao

    companion object {
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
