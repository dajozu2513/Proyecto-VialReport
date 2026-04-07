package com.vialreport.app.domain.usecase.report

import com.vialreport.app.domain.model.Report
import com.vialreport.app.domain.repository.IReportRepository
import javax.inject.Inject

class GetReportByIdUseCase @Inject constructor(
    private val repository: IReportRepository
) {
    suspend operator fun invoke(id: String): Report? = repository.getById(id)
}
