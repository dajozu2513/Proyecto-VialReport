package com.vialreport.app.domain.usecase.report

import com.vialreport.app.domain.model.Report
import com.vialreport.app.domain.repository.IReportRepository
import javax.inject.Inject

class CreateReportUseCase @Inject constructor(
    private val repository: IReportRepository
) {
    suspend operator fun invoke(
        typeId: String,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String
    ): Report = repository.create(typeId, title, description, latitude, longitude, address)
}
