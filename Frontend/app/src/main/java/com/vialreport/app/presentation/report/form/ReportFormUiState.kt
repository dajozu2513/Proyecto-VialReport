package com.vialreport.app.presentation.report.form

import com.vialreport.app.domain.model.IncidentType

data class ReportFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEdit: Boolean = false,
    val title: String = "",
    val description: String = "",
    val typeId: String = "",
    val status: String = "new",
    val address: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val incidentTypes: List<IncidentType> = emptyList(),
    val canSave: Boolean = false,
    val error: String? = null
)
