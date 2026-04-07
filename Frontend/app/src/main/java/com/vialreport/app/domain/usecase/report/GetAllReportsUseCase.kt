package com.vialreport.app.domain.usecase.report

import com.vialreport.app.domain.model.Report
import com.vialreport.app.domain.repository.IReportRepository
import javax.inject.Inject

class GetAllReportsUseCase @Inject constructor(
    private val repository: IReportRepository
) {
    suspend operator fun invoke(): List<Report> = repository.getAll()
}
