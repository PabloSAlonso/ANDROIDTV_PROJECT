package net.emite.androidtv_project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import net.emite.androidtv_project.data.local.dao.ConfigDao
import net.emite.androidtv_project.data.local.entity.ConfigEntity

@Database(entities = [ConfigEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao
}
