package net.emite.androidtv_project.domain.repository

import net.emite.androidtv_project.domain.model.RefreshResult
import net.emite.androidtv_project.domain.model.SlideshowConfig

interface SlideshowRepository {
    suspend fun getSlideshowConfig(instancia: String): Result<SlideshowConfig>
    suspend fun checkForUpdates(instancia: String): RefreshResult
    suspend fun getLocalCachedConfig(): SlideshowConfig?
}
