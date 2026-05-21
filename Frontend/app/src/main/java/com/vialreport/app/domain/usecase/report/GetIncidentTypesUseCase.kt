package com.vialreport.app.domain.usecase.report

import com.vialreport.app.domain.model.IncidentType
import com.vialreport.app.domain.repository.IReportRepository
import javax.inject.Inject

class GetIncidentTypesUseCase @Inject constructor(
    private val repository: IReportRepository
) {
    suspend operator fun invoke(): List<IncidentType> = repository.getIncidentTypes()
}
