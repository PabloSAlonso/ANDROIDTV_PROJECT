package net.emite.androidtv_project.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.emite.androidtv_project.data.repository_impl.ConfigRepositoryImpl
import net.emite.androidtv_project.data.repository_impl.SlideshowRepositoryImpl
import net.emite.androidtv_project.domain.repository.ConfigRepository
import net.emite.androidtv_project.domain.repository.SlideshowRepository
import javax.inject.Singleton

/**
 * Módulo de Hilt para vincular las interfaces de los repositorios con sus implementaciones.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Vincula la interfaz [ConfigRepository] con su implementación [ConfigRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindConfigRepository(
        configRepositoryImpl: ConfigRepositoryImpl
    ): ConfigRepository

    /**
     * Vincula la interfaz [SlideshowRepository] con su implementación [SlideshowRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindSlideshowRepository(
        slideshowRepositoryImpl: SlideshowRepositoryImpl
    ): SlideshowRepository
}
