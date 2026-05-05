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

@Singleton
class MediaCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    private val TAG = "MediaCacheManager"
    private val cacheDir = File(context.cacheDir, "slideshow_media").apply {
        if (!exists()) mkdirs()
    }

    /**
     * Descarga y cachea los items del slideshow.
     * @param items Lista de items a cachear.
     * @param onProgress Callback para informar del progreso (actual, total).
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

    private suspend fun downloadVideo(item: SlideshowItem) {
        val localFile = getLocalFileForItem(item)
        
        // Si el archivo ya existe, podríamos verificar el MD5 si es necesario
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

    private suspend fun preloadImage(item: SlideshowItem) {
        Log.d(TAG, "Precargando imagen con Coil: ${item.mediaUrl}")
        val request = ImageRequest.Builder(context)
            .data(item.mediaUrl)
            .build()
        // Coil se encarga de la caché interna (disco y memoria)
        context.imageLoader.execute(request)
    }

    fun getLocalFileForItem(item: SlideshowItem): File {
        val extension = item.mediaUrl.substringAfterLast('.', "tmp")
        return File(cacheDir, "${item.id}.$extension")
    }

    fun isItemCached(item: SlideshowItem): Boolean {
        if (item.type == MediaType.IMAGE) {
            // Para imágenes, confiamos en Coil, pero para este flujo 
            // asumimos que si pasó por cacheItems ya está "lista"
            return true 
        }
        val file = getLocalFileForItem(item)
        return file.exists()
    }

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
}
