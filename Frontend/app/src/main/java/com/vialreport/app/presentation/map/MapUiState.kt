package com.vialreport.app.presentation.map

import com.vialreport.app.data.remote.dto.MapPointDto

data class MapUiState(
    val isLoading: Boolean = true,
    val points: List<MapPointDto> = emptyList(),
    val error: String? = null,
    val selectedPoint: MapPointDto? = null
)
