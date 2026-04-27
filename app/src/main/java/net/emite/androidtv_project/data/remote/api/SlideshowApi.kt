package net.emite.androidtv_project.data.remote.api

import net.emite.androidtv_project.data.remote.dto.SlideshowResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface SlideshowApi {
    @GET
    suspend fun getSlideshow(@Url url: String): SlideshowResponse
}
