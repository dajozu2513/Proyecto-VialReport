package com.vialreport.app.presentation.report.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vialreport.app.data.local.TokenStore
import com.vialreport.app.domain.usecase.report.DeletePhotoUseCase
import com.vialreport.app.domain.usecase.report.GetReportByIdUseCase
import com.vialreport.app.domain.usecase.report.UpdateStatusUseCase
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
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val updateStatusUseCase: UpdateStatusUseCase,
    private val tokenStore: TokenStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reportId: String = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(ReportDetailUiState(isLoading = true, isAdmin = tokenStore.isAdmin))
    val uiState: StateFlow<ReportDetailUiState> = _uiState

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getReportByIdUseCase(reportId) }
                .onSuccess { report ->
                    _uiState.update {
                        if (report != null) it.copy(isLoading = false, report = report)
                        else it.copy(isLoading = false, error = "Reporte no encontrado")
                    }
                }
                .onFailure { e ->
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

    fun changeStatus(newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isChangingStatus = true, statusError = null) }
            runCatching { updateStatusUseCase(reportId, newStatus) }
                .onSuccess { updatedReport ->
                    _uiState.update { it.copy(isChangingStatus = false, report = updatedReport, statusSaved = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isChangingStatus = false, statusError = e.message ?: "Error al cambiar estado") }
                }
        }
    }

    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(deletingPhotoId = photoId, photoError = null) }
            runCatching { deletePhotoUseCase(reportId, photoId) }
                .onSuccess {
                    _uiState.update { it.copy(deletingPhotoId = null) }
                    loadReport()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(deletingPhotoId = null, photoError = e.message ?: "Error al eliminar foto") }
                }
        }
    }

    fun clearPhotoError()   = _uiState.update { it.copy(photoError = null) }
    fun clearStatusError()  = _uiState.update { it.copy(statusError = null) }
    fun clearStatusSaved()  = _uiState.update { it.copy(statusSaved = false) }
}
