package com.vialreport.app.presentation.report.form

data class ReportFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEdit: Boolean = false,
    val title: String = "",
    val description: String = "",
    val type: String = "pothole",
    val status: String = "new",
    val priority: String = "medium",
    val address: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val citizenName: String = "",
    val canSave: Boolean = false,
    val error: String? = null
)
