package net.emite.androidtv_project.domain.model

data class Config(
    val id: Int = 0,
    val instancia: String,
    val correo: String,
    val passwordHash: String
)
