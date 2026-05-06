package net.emite.androidtv_project.domain.model

sealed class RefreshResult {
    /** El JSON remoto es idéntico al local. No hay nada que hacer. */
    object NoChange : RefreshResult()

    /** El JSON ha cambiado. Config nueva lista para aplicar. */
    data class Updated(val config: SlideshowConfig) : RefreshResult()

    /** Fallo de red u otro error. Se proporciona el mensaje para el aviso. */
    data class NetworkError(val message: String) : RefreshResult()
}
