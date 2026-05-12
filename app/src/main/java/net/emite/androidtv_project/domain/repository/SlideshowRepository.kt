package net.emite.androidtv_project.domain.repository

import net.emite.androidtv_project.domain.model.RefreshResult
import net.emite.androidtv_project.domain.model.SlideshowConfig

/**
 * Interfaz que define las operaciones para la gestión de datos del slideshow.
 */
interface SlideshowRepository {
    /**
     * Obtiene la configuración del slideshow desde la fuente remota.
     * @param instancia Identificador de la instancia del dispositivo.
     * @return [Result] que contiene [SlideshowConfig] si tiene éxito o una excepción si falla.
     */
    suspend fun getSlideshowConfig(instancia: String): Result<SlideshowConfig>

    /**
     * Comprueba si hay actualizaciones en el servidor comparando con el estado local.
     * @param instancia Identificador de la instancia del dispositivo.
     * @return [RefreshResult] indicando si hubo cambios, si no los hubo o si ocurrió un error.
     */
    suspend fun checkForUpdates(instancia: String): RefreshResult

    /**
     * Obtiene la última configuración del slideshow almacenada localmente en caché.
     * @return [SlideshowConfig] si existe en caché, null en caso contrario.
     */
    suspend fun getLocalCachedConfig(): SlideshowConfig?
}
