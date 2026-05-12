package net.emite.androidtv_project.data.repository_impl

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import net.emite.androidtv_project.core.utils.DeviceUtils
import net.emite.androidtv_project.data.mapper.toDomainItem
import net.emite.androidtv_project.data.remote.api.SlideshowApi
import net.emite.androidtv_project.data.remote.dto.SlideshowResponse
import net.emite.androidtv_project.data.local.dao.CachedJsonDao
import net.emite.androidtv_project.data.local.entity.CachedJsonEntity
import net.emite.androidtv_project.domain.model.RefreshResult
import net.emite.androidtv_project.domain.model.SlideshowConfig
import net.emite.androidtv_project.domain.repository.SlideshowRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementación de [SlideshowRepository] que gestiona la sincronización de contenidos.
 * Combina peticiones a la API remota con una estrategia de caché local para permitir
 * el funcionamiento sin conexión.
 *
 * @property api Interfaz de acceso a la API remota.
 * @property cachedJsonDao DAO para la persistencia del JSON en la base de datos.
 * @property context Contexto de la aplicación.
 */
class SlideshowRepositoryImpl @Inject constructor(
    private val api: SlideshowApi,
    private val cachedJsonDao: CachedJsonDao,
    @ApplicationContext private val context: Context
) : SlideshowRepository {

    private val TAG = "SlideshowRepo"
    private val jsonParser = Json { ignoreUnknownKeys = true }

    /**
     * Obtiene la configuración del slideshow desde el servidor.
     * Si la petición falla, intenta recuperar la última configuración válida de la caché local.
     * 
     * @param instancia Identificador de la instancia del dispositivo.
     * @return [Result] con la configuración procesada.
     * @throws Exception "MAC_NOT_FOUND" si el servidor no reconoce el dispositivo.
     */
    override suspend fun getSlideshowConfig(instancia: String): Result<SlideshowConfig> {
        return try {
            val deviceId = DeviceUtils.getDeviceId(context)
            val url = "https://$instancia.tegestiona.es/pantallas/sync/$deviceId"
            Log.d(TAG, "ID de dispositivo detectado: $deviceId")
            Log.d(TAG, "Iniciando sincronización de pantallas: $url")

            val response = api.getSlideshow(url)
            val responseBody = response.body()?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(TAG, "Error HTTP ${response.code()}, intentando cargar desde caché local...")
                return loadFromCache(instancia)
            }

            if (responseBody.trim().equals("null", ignoreCase = true) || responseBody.isEmpty()) {
                throw Exception("MAC_NOT_FOUND")
            }

            // Guardar en caché local para uso offline
            cachedJsonDao.upsertCachedJson(
                CachedJsonEntity(
                    rawJson = responseBody,
                    lastSavedTimestamp = System.currentTimeMillis()
                )
            )

            val config = parseJsonToConfig(responseBody, instancia)
            Log.d(TAG, "Sincronización remota finalizada: ${config.items.size} diapositivas procesadas")
            Result.success(config)

        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización remota: ${e.message}, intentando cargar desde caché local...")
            if (e.message == "MAC_NOT_FOUND") return Result.failure(e)
            loadFromCache(instancia)
        }
    }

    /**
     * Comprueba si hay una nueva versión del JSON en el servidor.
     * Si el JSON es idéntico al almacenado localmente, no realiza cambios.
     * 
     * @param instancia Identificador de la instancia del dispositivo.
     * @return [RefreshResult] indicando el estado de la actualización.
     */
    override suspend fun checkForUpdates(instancia: String): RefreshResult {
        return try {
            val deviceId = DeviceUtils.getDeviceId(context)
            val url = "https://$instancia.tegestiona.es/pantallas/sync/$deviceId"
            
            val response = api.getSlideshow(url)
            val remoteJson = response.body()?.string() ?: return RefreshResult.NetworkError("Respuesta vacía")

            if (!response.isSuccessful) return RefreshResult.NetworkError("HTTP ${response.code()}")

            val localEntity = cachedJsonDao.getCachedJson()
            
            // Comparación de JSON raw (trim para ignorar whitespace)
            if (localEntity != null && localEntity.rawJson.trim() == remoteJson.trim()) {
                Log.d(TAG, "[REFRESH] JSON sin cambios. No se requiere actualización.")
                return RefreshResult.NoChange
            }

            // Hay cambios (o no había caché): guardar y parsear
            cachedJsonDao.upsertCachedJson(
                CachedJsonEntity(rawJson = remoteJson, lastSavedTimestamp = System.currentTimeMillis())
            )
            val config = parseJsonToConfig(remoteJson, instancia)
            Log.i(TAG, "[REFRESH] Cambios detectados. Config actualizada.")
            RefreshResult.Updated(config)

        } catch (e: Exception) {
            Log.w(TAG, "[REFRESH] Error durante comprobación: ${e.message}")
            RefreshResult.NetworkError(e.message ?: "Error desconocido")
        }
    }

    /**
     * Obtiene la configuración guardada localmente si existe.
     * @return [SlideshowConfig] o null si no hay caché disponible.
     */
    override suspend fun getLocalCachedConfig(): SlideshowConfig? {
        return try {
            val localEntity = cachedJsonDao.getCachedJson()
            if (localEntity != null) {
                parseJsonToConfig(localEntity.rawJson, "demo")
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Intenta cargar la configuración desde la base de datos local.
     */
    private suspend fun loadFromCache(instancia: String): Result<SlideshowConfig> {
        return try {
            val localEntity = cachedJsonDao.getCachedJson()
            if (localEntity != null) {
                val config = parseJsonToConfig(localEntity.rawJson, instancia)
                Log.i(TAG, "Cargada configuración desde caché local (${config.items.size} ítems)")
                Result.success(config)
            } else {
                Result.failure(Exception("Sin conexión y sin caché local disponible"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convierte el JSON crudo en un objeto de dominio [SlideshowConfig].
     */
    private suspend fun parseJsonToConfig(rawJson: String, instancia: String): SlideshowConfig = withContext(Dispatchers.Default) {
        val parsedResponse = jsonParser.decodeFromString<SlideshowResponse>(rawJson)
        val orientation = parsedResponse.cfg.orientacion ?: "H"
        val folder = parsedResponse.cfg.url ?: "demo"
        
        val mappedItems = parsedResponse.screens.values.map { screen ->
            screen.toDomainItem(instancia = instancia, folder = folder)
        }
        
        SlideshowConfig(orientation = orientation, items = mappedItems)
    }
}
