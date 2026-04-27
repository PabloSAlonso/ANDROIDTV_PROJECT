package net.emite.androidtv_project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val status: String? = null,
    val message: String? = null,
    val error: String? = null
)
