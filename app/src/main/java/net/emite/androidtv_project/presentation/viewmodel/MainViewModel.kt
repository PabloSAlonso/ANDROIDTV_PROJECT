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

    val isLoggedIn: StateFlow<Boolean?> = configRepository.getConfig()
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
