package com.vialreport.app.domain.usecase.report

import com.vialreport.app.domain.model.Report
import com.vialreport.app.domain.repository.IReportRepository
import javax.inject.Inject

class UpdateReportUseCase @Inject constructor(
    private val repository: IReportRepository
) {
    suspend operator fun invoke(
        id: String,
        typeId: String,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String
    ): Report = repository.update(id, typeId, title, description, latitude, longitude, address)
}
