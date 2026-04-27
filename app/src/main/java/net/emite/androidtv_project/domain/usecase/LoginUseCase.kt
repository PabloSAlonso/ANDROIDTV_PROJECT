package net.emite.androidtv_project.domain.usecase

import net.emite.androidtv_project.core.utils.HashUtils
import net.emite.androidtv_project.data.remote.dto.LoginResponse
import net.emite.androidtv_project.domain.model.Config
import net.emite.androidtv_project.domain.repository.AuthRepository
import net.emite.androidtv_project.domain.repository.ConfigRepository
import retrofit2.Response
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val configRepository: ConfigRepository
) {
    suspend operator fun invoke(
        instancia: String,
        correo: String,
        passwd: String
    ): Result<LoginResponse> {
        return try {
            val response = authRepository.login(instancia, correo, passwd)
            if (response.isSuccessful) {
                val body = response.body()
                // Si el status es exitoso (depende de la API, asumimos "success" o status no nulo)
                if (body?.status == "success" || body?.error == null) {
                    val config = Config(
                        instancia = instancia,
                        correo = correo,
                        passwordHash = HashUtils.sha256(passwd)
                    )
                    configRepository.saveConfig(config)
                    Result.success(body ?: LoginResponse(status = "success"))
                } else {
                    Result.failure(Exception(body?.message ?: body?.error ?: "Login fallido"))
                }
            } else {
                Result.failure(Exception("Error de red: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
