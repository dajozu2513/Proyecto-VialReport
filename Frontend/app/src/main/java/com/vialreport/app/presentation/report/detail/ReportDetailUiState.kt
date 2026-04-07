package com.vialreport.app.presentation.report.detail

import com.vialreport.app.domain.model.Report

data class ReportDetailUiState(
    val isLoading: Boolean = false,
    val report: Report? = null,
    val error: String? = null
)
