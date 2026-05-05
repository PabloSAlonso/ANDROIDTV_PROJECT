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
import net.emite.androidtv_project.core.utils.SlideshowSyncUtils
import net.emite.androidtv_project.core.utils.PhpSerializerUtils
import net.emite.androidtv_project.core.utils.MediaCacheManager
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
    private val mediaCacheManager: MediaCacheManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "SlideshowVM"

    private val _uiState = MutableStateFlow<SlideshowUiState>(SlideshowUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _currentItem = MutableStateFlow<SlideshowItem?>(null)
    val currentItem = _currentItem.asStateFlow()

    private val videoCompletionSignal = Channel<Unit>(Channel.CONFLATED)

    private var items: List<SlideshowItem> = emptyList()

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
                        
//                        // Si es la instancia de prueba 'demo', añadimos el video solicitado manualmente
//                        val testItems = if (config.instancia.lowercase() == "demo") {
//                            Log.d(TAG, "Añadiendo video de prueba manual para instancia demo")
//                            listOf(
//                                SlideshowItem(
//                                    id = "test_video_mp4",
//                                    mediaUrl = "https://demo.tegestiona.es/files/demo/t_pantallas_media/9_4_maspyme.mp4",
//                                    durationSeconds = 20,
//                                    type = MediaType.VIDEO,
//                                    orden = 999,
//                                    semana = null,
//                                    horas = null
//                                )
//                            )
//                        } else {
//                            emptyList()
//                        }

                        items = remoteItems
                        
                        if (items.isNotEmpty()) {
                            // Fase de precarga de recursos
                            viewModelScope.launch {
                                Log.d(TAG, "Iniciando fase de precarga de ${items.size} recursos...")
                                mediaCacheManager.cacheItems(items) { current, total ->
                                    _uiState.value = SlideshowUiState.Preloading(current, total)
                                }
                                Log.d(TAG, "Precarga finalizada. Iniciando slideshow...")
                                configRepository.saveLastUpdateTimestamp(System.currentTimeMillis())
                                _uiState.value = SlideshowUiState.Success(slideshowConfig.copy(items = items))
                                startSlideshowLoop()
                                startPeriodicUpdates(config.instancia)
                            }
                        } else {
                            Log.w(TAG, "La lista de diapositivas está vacía")
                            _uiState.value = SlideshowUiState.Success(slideshowConfig.copy(items = items))
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

                // ─── Log de auditoría de sincronización ────────────────────────────────────
                if (activeItems.isEmpty()) {
                    Log.w(TAG, "[SYNC] No hay ítems activos en este momento. Reintentando en 10s...")
                    delay(10000L)
                    continue
                }

                val totalDurationSec = SlideshowSyncUtils.calculateTotalCycleDuration(activeItems)
                val secondsSinceMidnight = SlideshowSyncUtils.getSecondsSinceMidnight()
                val currentTimeStr = SlideshowSyncUtils.getCurrentTimeString()
                val positionInCycle = secondsSinceMidnight % totalDurationSec

                Log.d(TAG, "[SYNC] ══════════════════════════════════════════")
                Log.d(TAG, "[SYNC] Calculando sincronización determinística...")
                Log.d(TAG, "[SYNC] Hora actual del sistema     : $currentTimeStr")
                Log.d(TAG, "[SYNC] Ítems activos en el ciclo   : ${activeItems.size}")
                Log.d(TAG, "[SYNC] Duración total del ciclo     : ${totalDurationSec}s")
                Log.d(TAG, "[SYNC] Segundos desde medianoche    : ${secondsSinceMidnight}s")
                Log.d(TAG, "[SYNC] Posición en el ciclo         : ${secondsSinceMidnight}s % ${totalDurationSec}s = ${positionInCycle}s")

                // ─── Cálculo del ítem actual ───────────────────────────────────────────────
                val syncResult = SlideshowSyncUtils.findCurrentSynchronizedItem(activeItems)

                if (syncResult == null) {
                    Log.w(TAG, "[SYNC] No se pudo calcular el ítem sincronizado. Reintentando en 5s...")
                    delay(5000L)
                    continue
                }

                val (currentItem, currentIndex, remainingSeconds, slotStart, slotEnd) = syncResult

                Log.d(TAG, "[SYNC] Ítem calculado               : [${currentIndex + 1}/${activeItems.size}] ID=${currentItem.id}")
                Log.d(TAG, "[SYNC] Slot temporal del ítem       : ${slotStart}s → ${slotEnd}s del ciclo")
                Log.d(TAG, "[SYNC] Tiempo restante para avanzar : ${remainingSeconds}s")
                Log.d(TAG, "[SYNC] URL del media                : ${currentItem.mediaUrl}")
                Log.d(TAG, "[SYNC] ══════════════════════════════════════════")

                // Precarga del siguiente ítem para transición suave
                val nextIndex = (currentIndex + 1) % activeItems.size
                preloadNextItem(activeItems[nextIndex])

                _currentItem.value = currentItem

                // ─── Espera el tiempo restante del slot actual ─────────────────────────────
                when (currentItem.type) {
                    MediaType.IMAGE -> {
                        delay(remainingSeconds * 1000L)
                    }
                    MediaType.VIDEO -> {
                        videoCompletionSignal.tryReceive() // Limpiar señales previas
                        // Para vídeos, respetamos el slot temporal del JSON para mantener sync global
                        kotlinx.coroutines.withTimeoutOrNull(remainingSeconds * 1000L) {
                            videoCompletionSignal.receive()
                        }
                        Log.d(TAG, "[SYNC] Vídeo: señal recibida o slot agotado para ID=${currentItem.id}")
                    }
                }
            }
        }
    }

    fun getLocalUri(item: SlideshowItem): String {
        return if (item.type == MediaType.VIDEO) {
            val file = mediaCacheManager.getLocalFileForItem(item)
            if (file.exists()) "file://${file.absolutePath}" else item.mediaUrl
        } else {
            item.mediaUrl
        }
    }

    fun onMediaVideoEnded() {
        videoCompletionSignal.trySend(Unit)
    }

    private fun startPeriodicUpdates(instancia: String) {
        viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val lastUpdate = configRepository.getLastUpdateTimestamp()
                
                if (shouldTriggerUpdate(currentTime, lastUpdate)) {
                    Log.i(TAG, "[UPDATE] Detectada ventana de actualización o catch-up necesario. Iniciando comprobación...")
                    performSilentUpdate(instancia)
                }
                
                // Comprobar cada 15 minutos si hemos entrado en una nueva ventana
                delay(15 * 60 * 1000L) 
            }
        }
    }

    private fun shouldTriggerUpdate(currentTimeMillis: Long, lastUpdateMillis: Long): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Ventanas: 0, 6, 12, 18
        val updateHours = listOf(0, 6, 12, 18)
        
        // Buscamos la última ventana teórica que debería haber pasado hoy
        val lastScheduledHour = updateHours.filter { it <= currentHour }.lastOrNull() ?: 0
        
        val lastScheduledCalendar = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(Calendar.HOUR_OF_DAY, lastScheduledHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val lastScheduledTime = lastScheduledCalendar.timeInMillis
        
        // Si la última actualización exitosa es anterior a la última ventana programada, toca actualizar
        return lastUpdateMillis < lastScheduledTime
    }

    private suspend fun performSilentUpdate(instancia: String) {
        try {
            val result = slideshowRepository.getSlideshowConfig(instancia)
            result.fold(
                onSuccess = { slideshowConfig ->
                    val newItems = slideshowConfig.items
                    Log.d(TAG, "[UPDATE] Nueva configuración descargada en segundo plano. Iniciando precarga silenciosa...")
                    
                    // Precarga silenciosa (no actualiza UI de progreso)
                    mediaCacheManager.cacheItems(newItems) { _, _ -> }
                    
                    // Actualización atómica de la lista de ítems para el siguiente ciclo del loop
                    items = newItems
                    configRepository.saveLastUpdateTimestamp(System.currentTimeMillis())
                    
                    Log.i(TAG, "[UPDATE] Actualización silenciosa completada con éxito.")
                },
                onFailure = {
                    Log.e(TAG, "[UPDATE] Fallo al descargar nueva configuración en segundo plano", it)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "[UPDATE] Error crítico durante actualización silenciosa", e)
        }
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
    data class Preloading(val current: Int, val total: Int) : SlideshowUiState()
    data class Success(val config: SlideshowConfig) : SlideshowUiState()
    data class Error(val message: String) : SlideshowUiState()
}
