package net.emite.androidtv_project.domain.repository

import net.emite.androidtv_project.data.remote.dto.LoginResponse
import retrofit2.Response

interface AuthRepository {
    suspend fun login(instancia: String, usuario: String, passwd: String): Response<LoginResponse>
}
