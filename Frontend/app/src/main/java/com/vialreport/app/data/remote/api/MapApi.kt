package com.vialreport.app.data.remote.api

import com.vialreport.app.data.remote.dto.ApiResponseDto
import com.vialreport.app.data.remote.dto.MapPointDto
import retrofit2.http.GET

interface MapApi {

    @GET("map/reports")
    suspend fun getMapPoints(): ApiResponseDto<List<MapPointDto>>
}
