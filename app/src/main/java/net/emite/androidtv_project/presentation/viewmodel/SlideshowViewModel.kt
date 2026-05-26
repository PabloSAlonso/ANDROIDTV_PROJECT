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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Calendar
import net.emite.androidtv_project.core.utils.SlideshowSyncUtils
import net.emite.androidtv_project.core.utils.PhpSerializerUtils
import net.emite.androidtv_project.core.utils.MediaCacheManager
import net.emite.androidtv_project.domain.model.MediaType
import net.emite.androidtv_project.domain.model.RefreshResult
import net.emite.androidtv_project.domain.model.SlideshowConfig
import net.emite.androidtv_project.domain.model.SlideshowItem
import net.emite.androidtv_project.domain.repository.ConfigRepository
import net.emite.androidtv_project.domain.repository.SlideshowRepository
import net.emite.androidtv_project.presentation.slideshow.guard.SystemRotationIntrusion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.imageLoader
import coil.request.ImageRequest
import javax.inject.Inject

/**
 * ViewModel que gestiona la lógica central del carrusel de diapositivas (slideshow).
 * Se encarga de la sincronización determinística, la precarga de medios y las actualizaciones silenciosas.
 * 
 * @property slideshowRepository Repositorio para datos del carrusel.
 * @property configRepository Repositorio para la configuración del dispositivo.
 * @property mediaCacheManager Gestor de la caché local de medios.
 * @property context Contexto de la aplicación.
 */
@HiltViewModel
class SlideshowViewModel @Inject constructor(
    private val slideshowRepository: SlideshowRepository,
    private val configRepository: ConfigRepository,
    private val mediaCacheManager: MediaCacheManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "SlideshowVM"

    private val _toastEvent = MutableSharedFlow<String>()
    /**
     * Canal para emitir mensajes Toast a la UI.
     */
    val toastEvent = _toastEvent.asSharedFlow()

    private var debugClickCount = 0
    private var debugClickJob: Job? = null

    private val _uiState = MutableStateFlow<SlideshowUiState>(SlideshowUiState.Loading())
    /**
     * Estado actual de la interfaz de usuario.
     */
    val uiState = _uiState.asStateFlow()

    private val _currentItem = MutableStateFlow<SlideshowItem?>(null)
    /**
     * Elemento multimedia que debe mostrarse actualmente.
     */
    val currentItem = _currentItem.asStateFlow()

    private val _orientation = MutableStateFlow("H")
    /**
     * Orientación de la pantalla ("H" o "V").
     */
    val orientation = _orientation.asStateFlow()

    /**
     * Flow derivado que indica si estamos en modo vertical de forma booleana (usado como source of truth por guard).
     */
    val isVerticalMode: StateFlow<Boolean> = _orientation
        .map { it == "V" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _sensorIntrusionCount = MutableStateFlow(0)
    val sensorIntrusionCount: StateFlow<Int> = _sensorIntrusionCount.asStateFlow()

    private val _lastIntrusion = MutableStateFlow<SystemRotationIntrusion>(
        SystemRotationIntrusion.None
    )
    val lastIntrusion: StateFlow<SystemRotationIntrusion> = _lastIntrusion.asStateFlow()

    fun reportSensorIntrusion(intrusion: SystemRotationIntrusion) {
        _sensorIntrusionCount.value += 1
        _lastIntrusion.value = intrusion
        Log.w(TAG, "Sensor Intrusion Detectada: $intrusion. Total: ${_sensorIntrusionCount.value}")
    }

    /**
     * Canal para recibir señales de finalización de video desde la UI.
     */
    private val videoCompletionSignal = Channel<Unit>(Channel.CONFLATED)

    /**
     * Lista completa de elementos recibidos del servidor.
     */
    private var items: List<SlideshowItem> = emptyList()

    init {
        loadSlideshow()
    }

    /**
     * Carga inicial de la configuración y el carrusel.
     */
    private fun loadSlideshow() {
        viewModelScope.launch {
            val config = configRepository.getConfig().firstOrNull()
            if (config != null) {
                _orientation.value = normalizeOrientation(config.orientation)
                Log.d(TAG, "Cargando slideshow para instancia: ${config.instancia}, orientación cacheada: ${config.orientation}")
                
                // 1. Priorizar datos en local antes que descargarlos
                val localConfig = slideshowRepository.getLocalCachedConfig(config.instancia)
                if (localConfig != null && localConfig.items.isNotEmpty()) {
                    Log.d(TAG, "Contenido local encontrado. Mostrando Splash Screen por 5s...")
                    items = localConfig.items
                    
                    // La splash Screen debe estar presente un minimo de tiempo para hacer publicidad de la app
                    _uiState.value = SlideshowUiState.Loading("Cargando imágenes...")
                    delay(5000L)
                    
                    _uiState.value = SlideshowUiState.Success(localConfig)
                    startSlideshowLoop()
                    
                    // Comprobar si hay cambios en segundo plano
                    viewModelScope.launch {
                        checkForUpdatesBackground(config.instancia)
                    }
                    
                    // Iniciar bucle de refresco periódico
                    startRefreshLoop(config.instancia)
                    return@launch
                }
                
                // 2. Si no hay datos locales, descargar mediante la web
                Log.d(TAG, "No hay contenido local. Descargando desde el servidor...")
                val result = slideshowRepository.getSlideshowConfig(config.instancia)
                result.fold(
                    onSuccess = { slideshowConfig ->
                        val remoteItems = slideshowConfig.items
                        items = remoteItems
                        
                        if (items.isNotEmpty()) {
                            // Fase de precarga de recursos
                            viewModelScope.launch {
                                Log.d(TAG, "Iniciando fase de precarga de ${items.size} recursos...")
                                mediaCacheManager.cacheItems(items) { current, total ->
                                    _uiState.value = SlideshowUiState.Preloading(current, total)
                                }
                                Log.d(TAG, "Precarga finalizada. Iniciando slideshow...")
                                mediaCacheManager.cleanUpUnusedMedia(items)
                                
                                // Actualizar orientación si ha cambiado en el JSON remoto
                                val normalizedRemoteOrientation = normalizeOrientation(slideshowConfig.orientation)
                                if (normalizedRemoteOrientation != _orientation.value) {
                                    _orientation.value = normalizedRemoteOrientation
                                    configRepository.saveConfig(config.copy(orientation = normalizedRemoteOrientation))
                                }

                                // Retardo visual para que el usuario vea el banner de carga finalizado
                                delay(3000L)
                                
                                _uiState.value = SlideshowUiState.Success(slideshowConfig.copy(items = items))
                                startSlideshowLoop()
                                startRefreshLoop(config.instancia)
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
                                message = "Este dispositivo no está registrado en la instancia indicada.\n\nIndique el siguiente código al administrador de Tegestiona para vincularlo:",
                                deviceId = deviceId
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

    /**
     * Comprueba si hay actualizaciones en segundo plano al iniciar con datos locales.
     */
    private suspend fun checkForUpdatesBackground(instancia: String) {
        Log.d(TAG, "[BACKGROUND_CHECK] Comprobando actualizaciones...")
        when (val result = slideshowRepository.checkForUpdates(instancia)) {
            is RefreshResult.Updated -> {
                Log.i(TAG, "[BACKGROUND_CHECK] Cambios detectados. Aplicando actualización silenciosa...")
                mediaCacheManager.cacheItems(result.config.items) { _, _ -> }
                mediaCacheManager.cleanUpUnusedMedia(result.config.items)
                
                items = result.config.items
                val currentState = _uiState.value
                if (currentState is SlideshowUiState.Success) {
                    _uiState.value = currentState.copy(
                        config = result.config,
                        networkWarning = null
                    )
                }
                Log.i(TAG, "[BACKGROUND_CHECK] Actualización silenciosa completada.")
            }
            is RefreshResult.NoChange -> {
                Log.d(TAG, "[BACKGROUND_CHECK] Sin cambios.")
            }
            is RefreshResult.NetworkError -> {
                Log.w(TAG, "[BACKGROUND_CHECK] Error de red: ${result.message}")
            }
        }
    }

    /**
     * Filtra los elementos activos basándose en la programación semanal y horaria.
     */
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

    /**
     * Precarga el siguiente elemento en segundo plano para una transición fluida.
     */
    private fun preloadNextItem(nextItem: SlideshowItem) {
        if (nextItem.type == MediaType.IMAGE) {
            Log.d(TAG, "Precargando siguiente imagen: ${nextItem.mediaUrl}")
            val request = ImageRequest.Builder(context)
                .data(nextItem.mediaUrl)
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    /**
     * Inicia el ciclo infinito de reproducción sincronizada.
     * Utiliza la hora del sistema para calcular el slot temporal actual.
     */
    private fun startSlideshowLoop() {
        viewModelScope.launch {
            while (true) {
                val activeItems = filterActiveItems(items)

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

                // Espera el tiempo restante del slot actual
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

    /**
     * Obtiene la URI local (para videos cacheados) o la remota (para imágenes o fallbacks).
     */
    fun getLocalUri(item: SlideshowItem): String {
        return if (item.type == MediaType.VIDEO) {
            val file = mediaCacheManager.getLocalFileForItem(item)
            if (file.exists()) "file://${file.absolutePath}" else item.mediaUrl
        } else {
            item.mediaUrl
        }
    }

    private fun normalizeOrientation(rawOrientation: String?): String {
        return if (rawOrientation?.uppercase() == "V") "V" else "H"
    }

    /**
     * Callback invocado por la vista cuando termina la reproducción de un video.
     */
    fun onMediaVideoEnded() {
        videoCompletionSignal.trySend(Unit)
    }

    /**
     * Inicia el bucle de actualización en segundo plano.
     * Comprueba si hay cambios en el JSON cada 15 minutos.
     */
    private fun startRefreshLoop(instancia: String) {
        viewModelScope.launch {
            while (true) {
                delay(15 * 60 * 1000L)
                Log.d(TAG, "[REFRESH] Iniciando comprobación de cambios en el JSON...")

                when (val result = slideshowRepository.checkForUpdates(instancia)) {
                    is RefreshResult.NoChange -> {
                        Log.d(TAG, "[REFRESH] Sin cambios. Slideshow continúa.")
                        val currentState = _uiState.value
                        if (currentState is SlideshowUiState.Success && currentState.networkWarning != null) {
                            _uiState.value = currentState.copy(networkWarning = null)
                        }
                    }
                    is RefreshResult.Updated -> {
                        Log.i(TAG, "[REFRESH] Cambios detectados. Aplicando actualización silenciosa...")
                        mediaCacheManager.cacheItems(result.config.items) { _, _ -> }
                        mediaCacheManager.cleanUpUnusedMedia(result.config.items)
                        
                        items = result.config.items
                        val currentState = _uiState.value
                        if (currentState is SlideshowUiState.Success) {
                            _uiState.value = currentState.copy(
                                config = result.config,
                                networkWarning = null
                            )
                        }
                        Log.i(TAG, "[REFRESH] Actualización silenciosa completada.")
                    }
                    is RefreshResult.NetworkError -> {
                        Log.w(TAG, "[REFRESH] Sin conexión: ${result.message}")
                        val currentState = _uiState.value
                        if (currentState is SlideshowUiState.Success) {
                            _uiState.value = currentState.copy(
                                networkWarning = "Sin conexión — mostrando contenido local"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Elimina la configuración local para volver a la pantalla de login/setup.
     */
    fun logout() {
        Log.i(TAG, "Ejecutando cierre de sesión (Logout)")
        viewModelScope.launch {
            configRepository.clearConfig()
        }
    }

    /**
     * Maneja el clic en el botón central/D-Pad del mando para depuración.
     * Al pulsar 5 veces, fuerza la descarga del JSON.
     */
    fun onCenterClick() {
        debugClickCount++
        viewModelScope.launch {
            if (debugClickCount == 5) {
                _toastEvent.emit("Descarga de JSON iniciada...")
                debugClickCount = 0
                debugClickJob?.cancel()
                forceRefresh()
            } else {
                _toastEvent.emit("Debes pulsarlo ${5 - debugClickCount} veces para forzar la descarga del JSON")
            }
        }

        // Resetear contador después de 2.5 segundos de inactividad
        debugClickJob?.cancel()
        debugClickJob = viewModelScope.launch {
            delay(2500)
            debugClickCount = 0
        }
    }

    /**
     * Fuerza la sincronización y descarga del JSON remoto.
     */
    private suspend fun forceRefresh() {
        val config = configRepository.getConfig().firstOrNull()
        if (config == null) {
            _toastEvent.emit("Error: No hay configuración guardada")
            return
        }
        
        Log.i(TAG, "[FORCE_REFRESH] Iniciando comprobación manual de actualización...")
        when (val result = slideshowRepository.checkForUpdates(config.instancia)) {
            is RefreshResult.Updated -> {
                Log.i(TAG, "[FORCE_REFRESH] Actualización encontrada. Descargando recursos...")
                mediaCacheManager.cacheItems(result.config.items) { _, _ -> }
                mediaCacheManager.cleanUpUnusedMedia(result.config.items)
                
                items = result.config.items
                val currentState = _uiState.value
                if (currentState is SlideshowUiState.Success) {
                    _uiState.value = currentState.copy(
                        config = result.config,
                        networkWarning = null
                    )
                }
                _toastEvent.emit("Descarga completada con éxito")
            }
            is RefreshResult.NoChange -> {
                Log.i(TAG, "[FORCE_REFRESH] Sin cambios en el servidor.")
                _toastEvent.emit("Descarga completada: no hay cambios en el JSON")
            }
            is RefreshResult.NetworkError -> {
                Log.e(TAG, "[FORCE_REFRESH] Error de red: ${result.message}")
                _toastEvent.emit("Error al descargar JSON: ${result.message}")
            }
        }
    }
}

/**
 * Estados posibles de la interfaz de usuario del slideshow.
 */
sealed class SlideshowUiState {
    /** Cargando configuración inicial. */
    data class Loading(val message: String = "Conectando con el servidor...") : SlideshowUiState()
    /** Descargando y preparando archivos multimedia. */
    data class Preloading(val current: Int, val total: Int) : SlideshowUiState()
    /** Slideshow activo y reproduciéndose correctamente. */
    data class Success(
        val config: SlideshowConfig,
        val networkWarning: String? = null
    ) : SlideshowUiState()
    /** Ocurrió un error crítico que impide la visualización. */
    data class Error(val message: String, val deviceId: String? = null) : SlideshowUiState()
}
