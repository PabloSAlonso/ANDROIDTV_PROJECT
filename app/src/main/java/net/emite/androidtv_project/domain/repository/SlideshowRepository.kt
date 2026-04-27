package net.emite.androidtv_project.domain.repository

import net.emite.androidtv_project.domain.model.SlideshowConfig

interface SlideshowRepository {
    suspend fun getSlideshowConfig(instancia: String): Result<SlideshowConfig>
}
