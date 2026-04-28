package net.emite.androidtv_project.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.emite.androidtv_project.domain.model.Config
import net.emite.androidtv_project.domain.repository.ConfigRepository
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    fun saveInstancia(instancia: String) {
        val trimmed = instancia.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            Log.d("SetupVM", "Guardando instancia: $trimmed")
            configRepository.saveConfig(Config(instancia = trimmed))
            _saved.value = true
        }
    }
}
