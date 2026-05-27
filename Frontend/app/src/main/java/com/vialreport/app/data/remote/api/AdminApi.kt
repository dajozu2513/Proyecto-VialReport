package com.vialreport.app.data.remote.api

import com.vialreport.app.data.remote.dto.AdminStatsDto
import com.vialreport.app.data.remote.dto.ApiResponseDto
import retrofit2.http.GET

interface AdminApi {

    @GET("admin/stats")
    suspend fun getStats(): ApiResponseDto<AdminStatsDto>
}
