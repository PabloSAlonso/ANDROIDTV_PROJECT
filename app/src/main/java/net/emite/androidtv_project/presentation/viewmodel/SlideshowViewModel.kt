package net.emite.androidtv_project.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import net.emite.androidtv_project.domain.model.MediaType
import net.emite.androidtv_project.domain.model.SlideshowConfig
import net.emite.androidtv_project.domain.model.SlideshowItem
import net.emite.androidtv_project.domain.repository.ConfigRepository
import net.emite.androidtv_project.domain.repository.SlideshowRepository
import javax.inject.Inject

@HiltViewModel
class SlideshowViewModel @Inject constructor(
    private val slideshowRepository: SlideshowRepository,
    private val configRepository: ConfigRepository
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
                        items = slideshowConfig.items
                        Log.d(TAG, "Slideshow cargado con éxito. Items: ${items.size}")
                        _uiState.value = SlideshowUiState.Success(slideshowConfig)
                        if (items.isNotEmpty()) {
                            startSlideshowLoop()
                        } else {
                            Log.w(TAG, "La lista de diapositivas está vacía")
                        }
                    },
                    onFailure = {
                        Log.e(TAG, "Fallo al cargar slideshow", it)
                        _uiState.value = SlideshowUiState.Error(it.message ?: "Error al cargar slideshow")
                    }
                )
            } else {
                Log.e(TAG, "No se encontró configuración en la base de datos")
                _uiState.value = SlideshowUiState.Error("No hay configuración guardada")
            }
        }
    }

    private fun startSlideshowLoop() {
        viewModelScope.launch {
            while (true) {
                if (items.isNotEmpty()) {
                    val item = items[currentIndex]
                    Log.d(
                        TAG,
                        "Mostrando media [${currentIndex + 1}/${items.size}]: ${item.mediaUrl} (Tipo: ${item.type}, Duración: ${item.durationSeconds}s)"
                    )
                    _currentItem.value = item

                    when (item.type) {
                        MediaType.IMAGE -> {
                            delay(item.durationSeconds * 1000L)
                        }
                        MediaType.VIDEO -> {
                            videoCompletionSignal.tryReceive()
                            videoCompletionSignal.receive()
                        }
                    }

                    currentIndex = (currentIndex + 1) % items.size
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
