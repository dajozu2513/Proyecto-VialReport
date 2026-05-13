package com.vialreport.backend.model

import com.vialreport.backend.dto.CrewResponse
import org.bson.types.ObjectId

data class Crew(
    val id: ObjectId = ObjectId(),
    val name: String,
    val zone: String,
    val available: Boolean = true
) {
    fun toResponse() = CrewResponse(
        id        = id.toHexString(),
        name      = name,
        zone      = zone,
        available = available
    )
}
