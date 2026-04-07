package com.vialreport.app.data.remote.mapper

import com.vialreport.app.data.remote.dto.ReportDto
import com.vialreport.app.data.remote.dto.ReportRequestDto
import com.vialreport.app.domain.model.Report

fun ReportDto.toDomain(): Report {
    return Report(
        id = id,
        title = title,
        description = description,
        type = type,
        status = status,
        priority = priority,
        address = address,
        latitude = latitude,
        longitude = longitude,
        citizenName = citizenName,
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: createdAt ?: ""
    )
}

fun Report.toRequestDto(): ReportRequestDto {
    return ReportRequestDto(
        title = title,
        description = description,
        type = type,
        status = status,
        priority = priority,
        address = address,
        latitude = latitude,
        longitude = longitude,
        citizenName = citizenName,
        updatedAt = updatedAt
    )
}
