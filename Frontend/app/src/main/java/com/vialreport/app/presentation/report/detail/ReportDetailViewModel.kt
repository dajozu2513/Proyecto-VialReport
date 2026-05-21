package com.vialreport.app.presentation.report.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vialreport.app.domain.usecase.report.GetReportByIdUseCase
import com.vialreport.app.domain.usecase.report.UploadPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val getReportByIdUseCase: GetReportByIdUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reportId: String = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(ReportDetailUiState(isLoading = true))
    val uiState: StateFlow<ReportDetailUiState> = _uiState

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val report = getReportByIdUseCase(reportId)
                _uiState.update {
                    if (report != null) it.copy(isLoading = false, report = report)
                    else it.copy(isLoading = false, error = "Reporte no encontrado")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar el reporte") }
            }
        }
    }

    fun uploadPhoto(imageBytes: ByteArray, mimeType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, photoError = null) }
            runCatching { uploadPhotoUseCase(reportId, imageBytes, mimeType) }
                .onSuccess {
                    _uiState.update { it.copy(isUploadingPhoto = false) }
                    loadReport()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUploadingPhoto = false, photoError = e.message ?: "Error al subir la foto") }
                }
        }
    }

    fun clearPhotoError() {
        _uiState.update { it.copy(photoError = null) }
    }
}
