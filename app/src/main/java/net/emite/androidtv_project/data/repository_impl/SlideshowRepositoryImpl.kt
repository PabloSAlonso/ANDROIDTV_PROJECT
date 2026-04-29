package net.emite.androidtv_project.data.repository_impl

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import net.emite.androidtv_project.core.utils.DeviceUtils
import net.emite.androidtv_project.data.mapper.toDomainItem
import net.emite.androidtv_project.data.remote.api.SlideshowApi
import net.emite.androidtv_project.data.remote.dto.SlideshowResponse
import net.emite.androidtv_project.domain.model.SlideshowConfig
import net.emite.androidtv_project.domain.repository.SlideshowRepository
import javax.inject.Inject

class SlideshowRepositoryImpl @Inject constructor(
    private val api: SlideshowApi,
    @ApplicationContext private val context: Context
) : SlideshowRepository {

    private val TAG = "SlideshowRepo"

    override suspend fun getSlideshowConfig(instancia: String): Result<SlideshowConfig> {
        return try {
            val mac = DeviceUtils.getMacAddress(context)
            val url = "https://$instancia.tegestiona.es/pantallas/sync/$mac"
            Log.d(TAG, "Iniciando sincronización de pantallas: $url")

            val response = api.getSlideshow(url)
            val responseBody = response.body()?.string() ?: ""

            if (!response.isSuccessful) {
                throw Exception("HTTP_ERROR: ${response.code()}")
            }

            if (responseBody.trim().equals("null", ignoreCase = true) || responseBody.isEmpty()) {
                throw Exception("MAC_NOT_FOUND")
            }

            val jsonParser = Json { ignoreUnknownKeys = true }
            val parsedResponse = jsonParser.decodeFromString<SlideshowResponse>(responseBody)

            val orientation = parsedResponse.cfg.orientacion ?: "H"
            val folder = parsedResponse.cfg.url ?: "demo"
            Log.d(TAG, "Configuración recibida: Orientación=$orientation, Carpeta=$folder")

            val items = parsedResponse.screens.values.map { screen ->
                screen.toDomainItem(instancia = instancia, folder = folder)
            }
            Log.d(TAG, "Sincronización finalizada: ${items.size} diapositivas procesadas")

            Result.success(SlideshowConfig(orientation = orientation, items = items))
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización", e)
            Result.failure(e)
        }
    }
}
