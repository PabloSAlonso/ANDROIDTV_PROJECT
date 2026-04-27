package net.emite.androidtv_project.data.remote.api

import net.emite.androidtv_project.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

interface AuthApi {
    @FormUrlEncoded
    @POST
    suspend fun login(
        @Url url: String,
        @Field("usuario") usuario: String,
        @Field("passwd") passwd: String
    ): Response<LoginResponse>
}
