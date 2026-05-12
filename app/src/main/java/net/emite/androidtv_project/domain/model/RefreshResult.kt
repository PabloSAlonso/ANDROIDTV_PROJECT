package net.emite.androidtv_project.domain.model

/**
 * Representa el resultado de una operación de refresco de configuración.
 */
sealed class RefreshResult {
    /** 
     * Indica que el JSON remoto es idéntico al local. 
     * No hay cambios que aplicar. 
     */
    object NoChange : RefreshResult()

    /** 
     * Indica que el JSON ha cambiado y se ha procesado correctamente. 
     * @property config La nueva configuración del slideshow lista para ser aplicada.
     */
    data class Updated(val config: SlideshowConfig) : RefreshResult()

    /** 
     * Indica que ocurrió un error durante el proceso de refresco (red, parseo, etc.).
     * @property message Descripción del error ocurrido.
     */
    data class NetworkError(val message: String) : RefreshResult()
}
