package net.emite.androidtv_project.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import net.emite.androidtv_project.core.utils.PhpSerializerUtils
import net.emite.androidtv_project.domain.model.MediaType
import net.emite.androidtv_project.domain.model.SlideshowConfig
import net.emite.androidtv_project.domain.model.SlideshowItem
import net.emite.androidtv_project.domain.repository.ConfigRepository
import net.emite.androidtv_project.domain.repository.SlideshowRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.imageLoader
import coil.request.ImageRequest
import javax.inject.Inject

@HiltViewModel
class SlideshowViewModel @Inject constructor(
    private val slideshowRepository: SlideshowRepository,
    private val configRepository: ConfigRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "SlideshowVM"

    private val _uiState = MutableStateFlow<SlideshowUiState>(SlideshowUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _currentItem = MutableStateFlow<SlideshowItem?>(null)
    val currentItem = _currentItem.asStateFlow()

    private val videoCompletionSignal = Channel<Unit>(Channel.CONFLATED)

    private var items: List<SlideshowItem> = emptyList()
    private var currentIndex = 0

    init {
        loadSlideshow()
    }

    private fun loadSlideshow() {
        viewModelScope.launch {
            val config = configRepository.getConfig().firstOrNull()
            if (config != null) {
                Log.d(TAG, "Cargando slideshow para instancia: ${config.instancia}")
                val result = slideshowRepository.getSlideshowConfig(config.instancia)
                result.fold(
                    onSuccess = { slideshowConfig ->
                        val remoteItems = slideshowConfig.items
                        
                        // Si es la instancia de prueba 'demo', añadimos el video solicitado manualmente
                        val testItems = if (config.instancia.lowercase() == "demo") {
                            Log.d(TAG, "Añadiendo video de prueba manual para instancia demo")
                            listOf(
                                SlideshowItem(
                                    id = "test_video_mp4",
                                    mediaUrl = "https://demo.tegestiona.es/files/demo/t_pantallas_media/9_4_maspyme.mp4",
                                    durationSeconds = 20, 
                                    type = MediaType.VIDEO,
                                    orden = 999,
                                    semana = null,
                                    horas = null
                                )
                            )
                        } else {
                            emptyList()
                        }

                        items = remoteItems + testItems
                        Log.d(TAG, "Slideshow cargado. Items totales: ${items.size} (Remotos: ${remoteItems.size}, Test: ${testItems.size})")
                        
                        _uiState.value = SlideshowUiState.Success(slideshowConfig.copy(items = items))
                        
                        if (items.isNotEmpty()) {
                            startSlideshowLoop()
                        } else {
                            Log.w(TAG, "La lista de diapositivas está vacía")
                        }
                    },
                    onFailure = {
                        Log.e(TAG, "Fallo al cargar slideshow", it)
                        if (it.message?.contains("MAC_NOT_FOUND") == true) {
                            val deviceId = net.emite.androidtv_project.core.utils.DeviceUtils.getDeviceId(context)
                            _uiState.value = SlideshowUiState.Error(
                                "No se ha podido sincronizar.\n\n" +
                                "La instancia podría ser incorrecta o el dispositivo no está autorizado.\n" +
                                "Por favor, contacte a soporte e indique este código de dispositivo:\n\n$deviceId"
                            )
                        } else {
                            _uiState.value = SlideshowUiState.Error(it.message ?: "Error al cargar slideshow")
                        }
                    }
                )
            } else {
                Log.e(TAG, "No se encontró configuración en la base de datos")
                _uiState.value = SlideshowUiState.Error("No hay configuración guardada")
            }
        }
    }

    private suspend fun filterActiveItems(items: List<SlideshowItem>): List<SlideshowItem> = withContext(Dispatchers.Default) {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val currentHour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))

        Log.d(TAG, "Filtrando ítems activos para Día: $dayOfWeek, Hora: $currentHour")

        items.filter { item ->
            val activeDays = PhpSerializerUtils.parsePhpStringArray(item.semana)
            val activeHours = PhpSerializerUtils.parsePhpStringArray(item.horas)

            val isDayActive = activeDays.isEmpty() || activeDays.contains(dayOfWeek.toString())
            val isHourActive = activeHours.isEmpty() || activeHours.contains(currentHour)

            isDayActive && isHourActive
        }.sortedBy { it.orden }.also {
            Log.d(TAG, "Ítems activos tras filtrado: ${it.size} de ${items.size}")
        }
    }

    private fun preloadNextItem(nextItem: SlideshowItem) {
        if (nextItem.type == MediaType.IMAGE) {
            Log.d(TAG, "Precargando siguiente imagen: ${nextItem.mediaUrl}")
            val request = ImageRequest.Builder(context)
                .data(nextItem.mediaUrl)
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    private fun startSlideshowLoop() {
        viewModelScope.launch {
            while (true) {
                val activeItems = filterActiveItems(items)
                if (activeItems.isEmpty()) {
                    Log.w(TAG, "No hay ítems activos en este momento. Reintentando en 10s...")
                    delay(10000L)
                    continue
                }

                // Reproducción secuencial respetando duración
                for (i in activeItems.indices) {
                    val item = activeItems[i]
                    
                    // Precarga del siguiente elemento
                    val nextIndex = (i + 1) % activeItems.size
                    preloadNextItem(activeItems[nextIndex])

                    Log.d(TAG, ">> Reproduciendo [${i + 1}/${activeItems.size}]: ${item.id} - ${item.mediaUrl} durante ${item.durationSeconds}s")
                    _currentItem.value = item

                    when (item.type) {
                        MediaType.IMAGE -> {
                            delay(item.durationSeconds * 1000L)
                        }
                        MediaType.VIDEO -> {
                            videoCompletionSignal.tryReceive() // Limpiar señales previas
                            // Esperar a que el vídeo termine o un timeout de seguridad (duración + 5s)
                            kotlinx.coroutines.withTimeoutOrNull((item.durationSeconds + 5) * 1000L) {
                                videoCompletionSignal.receive()
                            }
                            Log.d(TAG, "<< Vídeo finalizado o timeout alcanzado para ${item.id}")
                        }
                    }
                }
            }
        }
    }

    fun onMediaVideoEnded() {
        videoCompletionSignal.trySend(Unit)
    }

    fun logout() {
        Log.i(TAG, "Ejecutando cierre de sesión (Logout)")
        viewModelScope.launch {
            configRepository.clearConfig()
        }
    }
}

sealed class SlideshowUiState {
    object Loading : SlideshowUiState()
    data class Success(val config: SlideshowConfig) : SlideshowUiState()
    data class Error(val message: String) : SlideshowUiState()
}
