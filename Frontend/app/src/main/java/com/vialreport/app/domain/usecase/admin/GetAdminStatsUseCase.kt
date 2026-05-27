package com.vialreport.app.domain.usecase.admin

import com.vialreport.app.data.remote.api.AdminApi
import com.vialreport.app.data.remote.dto.AdminStatsDto
import javax.inject.Inject

class GetAdminStatsUseCase @Inject constructor(
    private val api: AdminApi
) {
    suspend operator fun invoke(): AdminStatsDto {
        val response = api.getStats()
        return response.data ?: error(response.message)
    }
}
