package net.emite.androidtv_project.core.utils

import android.content.Context
import android.util.Log
import coil.imageLoader
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.emite.androidtv_project.domain.model.MediaType
import net.emite.androidtv_project.domain.model.SlideshowItem
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de caché para recursos multimedia (imágenes y videos).
 * Se encarga de descargar videos a almacenamiento local y precargar imágenes usando Coil.
 * 
 * @property context Contexto de la aplicación.
 * @property okHttpClient Cliente HTTP para la descarga de archivos grandes (videos).
 */
@Singleton
class MediaCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    private val TAG = "MediaCacheManager"
    
    /** 
     * Directorio de caché específico para los medios del slideshow.
     */
    private val cacheDir = File(context.cacheDir, "slideshow_media").apply {
        if (!exists()) mkdirs()
    }

    /**
     * Descarga y cachea de forma asíncrona los elementos del slideshow.
     * 
     * @param items Lista de [SlideshowItem] a procesar.
     * @param onProgress Función callback que informa del progreso (elementos procesados, total).
     */
    suspend fun cacheItems(
        items: List<SlideshowItem>,
        onProgress: (Int, Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        val total = items.size
        items.forEachIndexed { index, item ->
            try {
                if (item.type == MediaType.VIDEO) {
                    downloadVideo(item)
                } else {
                    preloadImage(item)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cacheando item ${item.id}: ${e.message}")
            }
            onProgress(index + 1, total)
        }
    }

    /**
     * Descarga un video al almacenamiento local si no existe o si el MD5 no coincide.
     */
    private suspend fun downloadVideo(item: SlideshowItem) {
        val localFile = getLocalFileForItem(item)
        
        if (localFile.exists()) {
            if (item.md5 != null && verifyMd5(localFile, item.md5)) {
                Log.d(TAG, "Vídeo ya en caché y MD5 verificado: ${item.id}")
                return
            } else if (item.md5 == null) {
                Log.d(TAG, "Vídeo ya en caché (sin MD5): ${item.id}")
                return
            }
            Log.d(TAG, "MD5 no coincide para ${item.id}, re-descargando...")
        }

        Log.d(TAG, "Descargando vídeo: ${item.mediaUrl}")
        val request = Request.Builder().url(item.mediaUrl).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Error descarga: ${response.code}")
            
            val body = response.body ?: throw Exception("Cuerpo de respuesta vacío")
            body.byteStream().use { input ->
                FileOutputStream(localFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        Log.d(TAG, "Vídeo descargado: ${localFile.absolutePath}")
    }

    /**
     * Precarga una imagen utilizando la biblioteca Coil.
     */
    private suspend fun preloadImage(item: SlideshowItem) {
        Log.d(TAG, "Precargando imagen con Coil: ${item.mediaUrl}")
        val request = ImageRequest.Builder(context)
            .data(item.mediaUrl)
            .build()
        context.imageLoader.execute(request)
    }

    /**
     * Obtiene la referencia al archivo local donde se almacena (o almacenará) un item.
     * @param item El elemento del slideshow.
     * @return Objeto [File] apuntando a la ruta local.
     */
    fun getLocalFileForItem(item: SlideshowItem): File {
        val extension = item.mediaUrl.substringAfterLast('.', "tmp")
        return File(cacheDir, "${item.id}.$extension")
    }

    /**
     * Verifica si un elemento multimedia ya se encuentra disponible en la caché local.
     * @param item El elemento a comprobar.
     * @return true si está cacheado, false en caso contrario.
     */
    fun isItemCached(item: SlideshowItem): Boolean {
        if (item.type == MediaType.IMAGE) {
            return true 
        }
        val file = getLocalFileForItem(item)
        return file.exists()
    }

    /**
     * Verifica la integridad de un archivo comparando su MD5.
     */
    private fun verifyMd5(file: File, expectedMd5: String): Boolean {
        try {
            val digest = MessageDigest.getInstance("MD5")
            val bytes = file.readBytes()
            val hash = digest.digest(bytes).joinToString("") { "%02x".format(it) }
            return hash.equals(expectedMd5, ignoreCase = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error verificando MD5: ${e.message}")
            return false
        }
    }

    /**
     * Elimina del almacenamiento local los archivos de video que ya no están presentes en la lista activa.
     * Ayuda a mantener el uso de disco optimizado.
     * 
     * @param activeItems Lista de [SlideshowItem] actualmente configurados.
     */
    suspend fun cleanUpUnusedMedia(activeItems: List<SlideshowItem>) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Iniciando limpieza de medios obsoletos...")
        try {
            val activeIds = activeItems.map { it.id }.toSet()
            val localFiles = cacheDir.listFiles()
            
            localFiles?.forEach { file ->
                val fileId = file.nameWithoutExtension
                if (!activeIds.contains(fileId)) {
                    Log.d(TAG, "Eliminando archivo obsoleto: ${file.name}")
                    if (file.delete()) {
                        Log.d(TAG, "Archivo eliminado con éxito: ${file.name}")
                    } else {
                        Log.w(TAG, "No se pudo eliminar el archivo: ${file.name}")
                    }
                }
            }
            Log.d(TAG, "Limpieza de medios completada.")
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la limpieza de medios: ${e.message}")
        }
    }
}
