package net.emite.androidtv_project.data.repository_impl

import net.emite.androidtv_project.core.utils.DeviceUtils
import net.emite.androidtv_project.data.remote.api.SlideshowApi
import net.emite.androidtv_project.domain.model.SlideshowConfig
import net.emite.androidtv_project.domain.model.SlideshowItem
import net.emite.androidtv_project.domain.repository.SlideshowRepository
import javax.inject.Inject

class SlideshowRepositoryImpl @Inject constructor(
    private val api: SlideshowApi
) : SlideshowRepository {
    
    override suspend fun getSlideshowConfig(instancia: String): Result<SlideshowConfig> {
        return try {
            val mac = DeviceUtils.getMacAddress()
            val url = "https://$instancia.tegestiona.es/pantallas/sync/$mac"
            
            val response = api.getSlideshow(url)
            
            val orientation = response.cfg.orientacion ?: "H"
            val items = response.screens.values.map { screen ->
                SlideshowItem(
                    id = screen.id,
                    imageUrl = "https://$instancia.tegestiona.es/pantallas_imagenes/${screen.file}",
                    durationSeconds = screen.duracion?.toIntOrNull() ?: 10
                )
            }
            
            Result.success(SlideshowConfig(orientation = orientation, items = items))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
