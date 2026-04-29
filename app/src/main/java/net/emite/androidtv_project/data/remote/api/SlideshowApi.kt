package net.emite.androidtv_project.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface SlideshowApi {
    @GET
    suspend fun getSlideshow(@Url url: String): Response<ResponseBody>
}
