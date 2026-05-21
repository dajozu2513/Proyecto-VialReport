package com.vialreport.app.data.remote.api

import com.vialreport.app.data.remote.dto.ApiResponseDto
import com.vialreport.app.data.remote.dto.IncidentTypeDto
import com.vialreport.app.data.remote.dto.ReportDto
import com.vialreport.app.data.remote.dto.ReportRequestDto
import com.vialreport.app.data.remote.dto.UpdateStatusRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ReportApi {

    @GET("reports")
    suspend fun getReports(): ApiResponseDto<List<ReportDto>>

    @GET("reports/{id}")
    suspend fun getReportById(@Path("id") id: String): ApiResponseDto<ReportDto>

    @POST("reports")
    suspend fun createReport(@Body request: ReportRequestDto): ApiResponseDto<ReportDto>

    @PUT("reports/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: String,
        @Body request: UpdateStatusRequestDto
    ): ApiResponseDto<ReportDto>

    @DELETE("reports/{id}")
    suspend fun deleteReport(@Path("id") id: String): ApiResponseDto<Unit>

    @GET("incident-types")
    suspend fun getIncidentTypes(): ApiResponseDto<List<IncidentTypeDto>>
}
