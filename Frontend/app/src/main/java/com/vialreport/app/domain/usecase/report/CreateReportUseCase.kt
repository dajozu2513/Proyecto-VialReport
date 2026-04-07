package com.vialreport.app.domain.usecase.report

import com.vialreport.app.domain.model.Report
import com.vialreport.app.domain.repository.IReportRepository
import javax.inject.Inject

class CreateReportUseCase @Inject constructor(
    private val repository: IReportRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        type: String,
        status: String,
        priority: String,
        address: String,
        latitude: Double,
        longitude: Double,
        citizenName: String
    ): Report = repository.create(
        title = title,
        description = description,
        type = type,
        status = status,
        priority = priority,
        address = address,
        latitude = latitude,
        longitude = longitude,
        citizenName = citizenName
    )
}
