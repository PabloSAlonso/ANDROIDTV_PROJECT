package net.emite.androidtv_project.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.emite.androidtv_project.domain.repository.ConfigRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {

    /**
     * null = cargando, false = sin instancia configurada, true = instancia guardada → ir al Slideshow
     */
    val hasInstance: StateFlow<Boolean?> = configRepository.getConfig()
        .map { config -> config?.instancia?.isNotBlank() == true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
