package com.vialreport.app.presentation.report.list

import com.vialreport.app.domain.model.Report

data class ReportListUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val selectedStatus: String = "",
    val items: List<Report> = emptyList(),
    val error: String? = null
)
