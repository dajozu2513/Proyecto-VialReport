package com.vialreport.app.presentation.report.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vialreport.app.domain.model.Report
import com.vialreport.app.domain.usecase.report.DeleteReportUseCase
import com.vialreport.app.domain.usecase.report.GetAllReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportListViewModel @Inject constructor(
    private val getAllReportsUseCase: GetAllReportsUseCase,
    private val deleteReportUseCase: DeleteReportUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_QUERY = "query"
        private const val KEY_STATUS = "status"
    }

    private val query = MutableStateFlow(savedStateHandle[KEY_QUERY] ?: "")
    private val selectedStatus = MutableStateFlow(savedStateHandle[KEY_STATUS] ?: "")
    private val items = MutableStateFlow(emptyList<Report>())
    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ReportListUiState> = combine(
        loading, query, selectedStatus, items, error
    ) { isLoading, currentQuery, currentStatus, currentItems, currentError ->

        val filtered = currentItems
            .filter { report ->
                (currentQuery.isBlank() ||
                        report.title.contains(currentQuery, ignoreCase = true) ||
                        report.address.contains(currentQuery, ignoreCase = true) ||
                        report.citizenName.contains(currentQuery, ignoreCase = true))
                        &&
                        (currentStatus.isBlank() || report.status == currentStatus)
            }

        ReportListUiState(
            isLoading = isLoading,
            query = currentQuery,
            selectedStatus = currentStatus,
            items = filtered,
            error = currentError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportListUiState(isLoading = true)
    )

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            try {
                items.value = getAllReportsUseCase()
            } catch (e: Exception) {
                error.value = e.message ?: "Error al cargar los reportes"
            } finally {
                loading.value = false
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        savedStateHandle[KEY_QUERY] = newQuery
        query.value = newQuery
    }

    fun onStatusFilterChange(status: String) {
        savedStateHandle[KEY_STATUS] = status
        selectedStatus.value = status
    }

    fun deleteReport(id: String) {
        viewModelScope.launch {
            try {
                deleteReportUseCase(id)
                loadReports()
            } catch (e: Exception) {
                error.value = e.message ?: "Error al eliminar el reporte"
            }
        }
    }
}
