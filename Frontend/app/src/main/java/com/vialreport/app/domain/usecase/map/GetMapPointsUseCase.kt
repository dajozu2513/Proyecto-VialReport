package com.vialreport.app.domain.usecase.map

import com.vialreport.app.data.remote.api.MapApi
import com.vialreport.app.data.remote.dto.MapPointDto
import javax.inject.Inject

class GetMapPointsUseCase @Inject constructor(
    private val api: MapApi
) {
    suspend operator fun invoke(): List<MapPointDto> {
        val response = api.getMapPoints()
        return response.data ?: emptyList()
    }
}
