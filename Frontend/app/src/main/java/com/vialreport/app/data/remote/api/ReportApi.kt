package com.vialreport.app.data.remote.api

import com.vialreport.app.data.remote.dto.ReportDto
import com.vialreport.app.data.remote.dto.ReportRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ReportApi {

    @GET("reports")
    suspend fun getReports(): List<ReportDto>

    @GET("reports/{id}")
    suspend fun getReportById(@Path("id") id: String): ReportDto

    @POST("reports")
    suspend fun createReport(@Body request: ReportRequestDto): ReportDto

    @PUT("reports/{id}")
    suspend fun updateReport(
        @Path("id") id: String,
        @Body request: ReportRequestDto
    ): ReportDto

    @DELETE("reports/{id}")
    suspend fun deleteReport(@Path("id") id: String)
}
