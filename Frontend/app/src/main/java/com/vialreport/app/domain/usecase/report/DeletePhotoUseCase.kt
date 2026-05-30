package com.vialreport.app.domain.usecase.report

import com.vialreport.app.domain.repository.IReportRepository
import javax.inject.Inject

class DeletePhotoUseCase @Inject constructor(
    private val repository: IReportRepository
) {
    suspend operator fun invoke(reportId: String, photoId: String) =
        repository.deletePhoto(reportId, photoId)
}
