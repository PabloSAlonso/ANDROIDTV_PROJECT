package net.emite.androidtv_project.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Interfaz de Retrofit para definir las peticiones de red relacionadas con el slideshow.
 */
interface SlideshowApi {
    /**
     * Realiza una petición GET a una URL dinámica para obtener el JSON de configuración.
     * Se utiliza [ResponseBody] porque el formato puede requerir un procesamiento personalizado
     * debido a la serialización de PHP.
     *
     * @param url URL completa de la petición.
     * @return [Response] que contiene el cuerpo de la respuesta en crudo.
     */
    @GET
    suspend fun getSlideshow(@Url url: String): Response<ResponseBody>
}
