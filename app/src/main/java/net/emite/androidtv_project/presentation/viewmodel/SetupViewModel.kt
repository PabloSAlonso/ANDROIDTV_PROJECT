package net.emite.androidtv_project.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.emite.androidtv_project.domain.model.Config
import net.emite.androidtv_project.domain.repository.ConfigRepository
import javax.inject.Inject

/**
 * ViewModel encargado de la lógica de la pantalla de configuración inicial.
 * Permite al usuario definir la instancia del dispositivo y ajustar la orientación de pantalla.
 * 
 * @property configRepository Repositorio para la gestión de la configuración local.
 */
@HiltViewModel
class SetupViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _saved = MutableStateFlow(false)
    /**
     * Flujo que emite true cuando los cambios han sido persistidos correctamente.
     */
    val saved = _saved.asStateFlow()

    /**
     * Flujo con la configuración actual cargada desde la base de datos.
     */
    val config: StateFlow<Config?> = configRepository.getConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Guarda el nombre de la instancia configurada por el usuario.
     * @param instancia El nombre de la instancia (ej. "emite").
     */
    fun saveInstancia(instancia: String) {
        val trimmed = instancia.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val currentConfig = config.value ?: Config(instancia = trimmed)
            configRepository.saveConfig(currentConfig.copy(instancia = trimmed))
            _saved.value = true
        }
    }

    /**
     * Alterna entre modo horizontal y vertical.
     * Al desactivar el modo vertical, se resetea automáticamente el modo invertido.
     */
    fun toggleVerticalMode() {
        viewModelScope.launch {
            val current = config.value ?: Config(instancia = "")
            val newVertical = !current.isVertical
            val newInverted = if (!newVertical) false else current.isInverted
            val newOrientation = if (newVertical) "V" else "H"
            Log.d("SetupVM", "Toggling vertical: $newVertical (inverted: $newInverted)")
            configRepository.saveConfig(current.copy(
                isVertical = newVertical, 
                isInverted = newInverted,
                orientation = newOrientation
            ))
        }
    }

    /**
     * Alterna entre orientación vertical normal e invertida (180 grados).
     * Solo tiene efecto si el modo vertical está activo.
     */
    fun toggleInvertedMode() {
        viewModelScope.launch {
            val current = config.value ?: Config(instancia = "")
            if (current.isVertical) {
                val newInverted = !current.isInverted
                Log.d("SetupVM", "Toggling inverted: $newInverted")
                configRepository.saveConfig(current.copy(isInverted = newInverted))
            }
        }
    }
}
