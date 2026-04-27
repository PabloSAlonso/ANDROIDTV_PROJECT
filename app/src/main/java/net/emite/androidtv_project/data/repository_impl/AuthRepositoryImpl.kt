package net.emite.androidtv_project.data.repository_impl

import net.emite.androidtv_project.data.remote.api.AuthApi
import net.emite.androidtv_project.data.remote.dto.LoginResponse
import net.emite.androidtv_project.domain.repository.AuthRepository
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi
) : AuthRepository {
    override suspend fun login(
        instancia: String,
        usuario: String,
        passwd: String
    ): Response<LoginResponse> {
        val url = "https://$instancia.tegestiona.es/login/json"
        return api.login(url, usuario, passwd)
    }
}
