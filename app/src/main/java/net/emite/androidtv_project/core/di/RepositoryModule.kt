package net.emite.androidtv_project.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.emite.androidtv_project.data.repository_impl.AuthRepositoryImpl
import net.emite.androidtv_project.data.repository_impl.ConfigRepositoryImpl
import net.emite.androidtv_project.data.repository_impl.SlideshowRepositoryImpl
import net.emite.androidtv_project.domain.repository.AuthRepository
import net.emite.androidtv_project.domain.repository.ConfigRepository
import net.emite.androidtv_project.domain.repository.SlideshowRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindConfigRepository(
        configRepositoryImpl: ConfigRepositoryImpl
    ): ConfigRepository

    @Binds
    @Singleton
    abstract fun bindSlideshowRepository(
        slideshowRepositoryImpl: SlideshowRepositoryImpl
    ): SlideshowRepository
}
