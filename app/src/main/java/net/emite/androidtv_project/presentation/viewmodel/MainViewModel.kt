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

/**
 * ViewModel principal que orquesta el estado global de la aplicación.
 * Determina si el dispositivo tiene una configuración válida para decidir la pantalla inicial.
 * 
 * @property configRepository Repositorio para acceder a la configuración persistente.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {

    /**
     * Estado que indica si existe una instancia configurada.
     * - `null`: Cargando estado inicial.
     * - `false`: No hay instancia (mostrar pantalla de configuración).
     * - `true`: Hay una instancia válida (proceder al slideshow).
     */
    val hasInstance: StateFlow<Boolean?> = configRepository.getConfig()
        .map { config -> config?.instancia?.isNotBlank() == true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Flujo de datos con la configuración completa del dispositivo.
     */
    val config = configRepository.getConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
