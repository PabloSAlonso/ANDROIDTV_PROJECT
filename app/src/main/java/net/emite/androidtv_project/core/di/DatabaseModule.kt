package net.emite.androidtv_project.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.emite.androidtv_project.data.local.AppDatabase
import net.emite.androidtv_project.data.local.dao.ConfigDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "androidtv_db"
        ).build()
    }

    @Provides
    fun provideConfigDao(db: AppDatabase): ConfigDao = db.configDao()
}
