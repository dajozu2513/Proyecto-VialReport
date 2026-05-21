package com.vialreport.app.domain.usecase.report

import com.vialreport.app.domain.model.ReportPhoto
import com.vialreport.app.domain.repository.IReportRepository
import javax.inject.Inject

class UploadPhotoUseCase @Inject constructor(
    private val repository: IReportRepository
) {
    suspend operator fun invoke(reportId: String, imageBytes: ByteArray, mimeType: String): ReportPhoto =
        repository.uploadPhoto(reportId, imageBytes, mimeType)
}
