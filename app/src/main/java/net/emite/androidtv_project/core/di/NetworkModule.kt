package net.emite.androidtv_project.core.di

import okhttp3.MediaType.Companion.toMediaType
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import net.emite.androidtv_project.data.remote.api.SlideshowApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo de Hilt para la provisión de dependencias relacionadas con la red.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provee el cliente HTTP configurado con tiempos de espera adecuados.
     * @return Instancia de [OkHttpClient].
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provee la instancia de Retrofit configurada para la API.
     * Utiliza kotlinx.serialization para el parseo de JSON.
     * 
     * @param okHttpClient Cliente HTTP para realizar las peticiones.
     * @return Instancia de [Retrofit].
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val json = Json { ignoreUnknownKeys = true }
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://placeholder.tegestiona.es/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    /**
     * Provee la interfaz de la API del slideshow.
     */
    @Provides
    @Singleton
    fun provideSlideshowApi(retrofit: Retrofit): SlideshowApi = retrofit.create(SlideshowApi::class.java)
}
