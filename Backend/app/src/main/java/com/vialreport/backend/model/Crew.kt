package com.vialreport.backend.model

import com.vialreport.backend.dto.CrewResponse
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object Crews : IntIdTable("crews") {
    val name      = varchar("name", 100)
    val zone      = varchar("zone", 100)
    val available = bool("available").default(true)
}
data class Crew(
    val id: Int,
    val name: String,
    val zone: String,
    val available: Boolean
) {
    fun toResponse() = CrewResponse(
        id        = id,
        name      = name,
        zone      = zone,
        available = available
    )
}