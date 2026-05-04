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

    private fun filterActiveItems(items: List<SlideshowItem>): List<SlideshowItem> {
        val calendar = Calendar.getInstance()
        // Calendar.DAY_OF_WEEK: 1=Sun, 2=Mon...7=Sat
        // PHP array for days: "0"=Sun, "1"=Mon..."6"=Sat
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val currentHour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))

        return items.filter { item ->
            val activeDays = PhpSerializerUtils.parsePhpStringArray(item.semana)
            val activeHours = PhpSerializerUtils.parsePhpStringArray(item.horas)

            val isDayActive = activeDays.isEmpty() || activeDays.contains(dayOfWeek.toString())
            val isHourActive = activeHours.isEmpty() || activeHours.contains(currentHour)

            isDayActive && isHourActive
        }.sortedBy { it.orden }
    }

    private fun startSlideshowLoop() {
        viewModelScope.launch {
            while (true) {
                val activeItems = filterActiveItems(items)
                if (activeItems.isEmpty()) {
                    Log.d(TAG, "Sincronización: No hay items activos en este momento, reintentando en 60s")
                    delay(60000L)
                    continue
                }

                val totalLoopDuration = activeItems.sumOf { it.durationSeconds }
                if (totalLoopDuration == 0) {
                    delay(5000L)
                    continue
                }

                val calendar = Calendar.getInstance()
                val secondsOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 3600 +
                        calendar.get(Calendar.MINUTE) * 60 +
                        calendar.get(Calendar.SECOND)

                val positionInLoop = secondsOfDay % totalLoopDuration

                var accumulatedTime = 0
                var targetItem: SlideshowItem? = null
                var timeLeftForItem = 0

                for (item in activeItems) {
                    accumulatedTime += item.durationSeconds
                    if (accumulatedTime > positionInLoop) {
                        targetItem = item
                        timeLeftForItem = accumulatedTime - positionInLoop
                        break
                    }
                }

                if (targetItem != null) {
                    Log.d(
                        TAG,
                        "Sincronización: Mostrando media ${targetItem.mediaUrl} por ${timeLeftForItem}s (Posición de bucle: $positionInLoop / $totalLoopDuration)"
                    )
                    _currentItem.value = targetItem

                    when (targetItem.type) {
                        MediaType.IMAGE -> {
                            delay(timeLeftForItem * 1000L)
                        }
                        MediaType.VIDEO -> {
                            videoCompletionSignal.tryReceive() // Limpiar señal vieja
                            kotlinx.coroutines.withTimeoutOrNull(timeLeftForItem * 1000L) {
                                videoCompletionSignal.receive()
                            }
                        }
                    }
                } else {
                    delay(5000L)
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
