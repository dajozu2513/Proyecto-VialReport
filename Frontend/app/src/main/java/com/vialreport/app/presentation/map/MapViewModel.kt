package com.vialreport.app.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vialreport.app.data.remote.dto.MapPointDto
import com.vialreport.app.domain.usecase.map.GetMapPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getMapPointsUseCase: GetMapPointsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    init {
        loadPoints()
    }

    fun loadPoints() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getMapPointsUseCase() }
                .onSuccess { points ->
                    _uiState.update { it.copy(isLoading = false, points = points) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar el mapa") }
                }
        }
    }

    fun selectPoint(point: MapPointDto?) = _uiState.update { it.copy(selectedPoint = point) }
}
