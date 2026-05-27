package com.vialreport.app.presentation.admin

import com.vialreport.app.data.remote.dto.AdminStatsDto

data class AdminStatsUiState(
    val isLoading: Boolean = true,
    val stats: AdminStatsDto? = null,
    val error: String? = null
)
