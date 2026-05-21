package com.vialreport.app.data.remote.mapper

import com.vialreport.app.data.remote.dto.ReportDto
import com.vialreport.app.domain.model.Report

fun ReportDto.toDomain(): Report = Report(
    id          = id,
    title       = title,
    description = description,
    type        = type.name,
    status      = status,
    priority    = priority,
    address     = address,
    latitude    = latitude,
    longitude   = longitude,
    citizenName = citizen.name,
    createdAt   = createdAt ?: "",
    updatedAt   = updatedAt ?: createdAt ?: ""
)
