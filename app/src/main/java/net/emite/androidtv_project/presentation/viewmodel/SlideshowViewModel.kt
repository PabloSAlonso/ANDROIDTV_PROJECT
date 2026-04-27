package net.emite.androidtv_project.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.emite.androidtv_project.domain.model.SlideshowConfig
import net.emite.androidtv_project.domain.model.SlideshowItem
import net.emite.androidtv_project.domain.repository.ConfigRepository
import net.emite.androidtv_project.domain.repository.SlideshowRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@HiltViewModel
class SlideshowViewModel @Inject constructor(
    private val slideshowRepository: SlideshowRepository,
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SlideshowUiState>(SlideshowUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _currentItem = MutableStateFlow<SlideshowItem?>(null)
    val currentItem = _currentItem.asStateFlow()

    private var items: List<SlideshowItem> = emptyList()
    private var currentIndex = 0

    init {
        loadSlideshow()
    }

    private fun loadSlideshow() {
        viewModelScope.launch {
            val config = configRepository.getConfig().firstOrNull()
            if (config != null) {
                val result = slideshowRepository.getSlideshowConfig(config.instancia)
                result.fold(
                    onSuccess = { slideshowConfig ->
                        items = slideshowConfig.items
                        _uiState.value = SlideshowUiState.Success(slideshowConfig)
                        if (items.isNotEmpty()) {
                            startSlideshowLoop()
                        }
                    },
                    onFailure = {
                        _uiState.value = SlideshowUiState.Error(it.message ?: "Error al cargar slideshow")
                    }
                )
            } else {
                _uiState.value = SlideshowUiState.Error("No hay configuración guardada")
            }
        }
    }

    private fun startSlideshowLoop() {
        viewModelScope.launch {
            while (true) {
                val item = items[currentIndex]
                _currentItem.value = item
                delay(item.durationSeconds * 1000L)
                currentIndex = (currentIndex + 1) % items.size
            }
        }
    }
}

sealed class SlideshowUiState {
    object Loading : SlideshowUiState()
    data class Success(val config: SlideshowConfig) : SlideshowUiState()
    data class Error(val message: String) : SlideshowUiState()
}
