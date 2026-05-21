package com.vialreport.app.domain.usecase.report

import com.vialreport.app.domain.model.Report
import com.vialreport.app.domain.repository.IReportRepository
import javax.inject.Inject

class UpdateStatusUseCase @Inject constructor(
    private val repository: IReportRepository
) {
    suspend operator fun invoke(id: String, status: String): Report =
        repository.updateStatus(id, status)
}
