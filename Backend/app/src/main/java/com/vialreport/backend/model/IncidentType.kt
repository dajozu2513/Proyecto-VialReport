package com.vialreport.backend.model

import com.vialreport.backend.dto.IncidentTypeResponse
import org.bson.types.ObjectId

data class IncidentType(
    val id: ObjectId = ObjectId(),
    val name: String,
    val icon: String,
    val color: String,
    val defaultPriority: Int = 2
) {
    fun toResponse() = IncidentTypeResponse(
        id              = id.toHexString(),
        name            = name,
        icon            = icon,
        color           = color,
        defaultPriority = defaultPriority
    )
}
