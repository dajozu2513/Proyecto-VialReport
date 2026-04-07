package com.vialreport.app.presentation.report.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vialreport.app.domain.usecase.report.GetReportByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val getReportByIdUseCase: GetReportByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reportId: String = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(ReportDetailUiState(isLoading = true))
    val uiState: StateFlow<ReportDetailUiState> = _uiState

    init {
        loadReport()
    }

    private fun loadReport() {
        viewModelScope.launch {
            _uiState.value = ReportDetailUiState(isLoading = true)
            try {
                val report = getReportByIdUseCase(reportId)
                _uiState.value = if (report != null) {
                    ReportDetailUiState(report = report)
                } else {
                    ReportDetailUiState(error = "Reporte no encontrado")
                }
            } catch (e: Exception) {
                _uiState.value = ReportDetailUiState(error = e.message ?: "Error al cargar el reporte")
            }
        }
    }
}
