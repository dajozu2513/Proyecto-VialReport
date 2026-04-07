package com.vialreport.app.domain.usecase.report

import com.vialreport.app.domain.repository.IReportRepository
import javax.inject.Inject

class DeleteReportUseCase @Inject constructor(
    private val repository: IReportRepository
) {
    suspend operator fun invoke(id: String): Boolean = repository.delete(id)
}
