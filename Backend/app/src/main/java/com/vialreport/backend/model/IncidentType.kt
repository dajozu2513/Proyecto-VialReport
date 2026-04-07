package com.vialreport.backend.model

import com.vialreport.backend.dto.IncidentTypeResponse
import org.jetbrains.exposed.sql.Table

object IncidentTypes : Table("incident_types") {
    val id              = integer("id").autoIncrement()
    val name            = varchar("name", 100)
    val icon            = varchar("icon", 50)
    val color           = varchar("color", 20)
    val defaultPriority = integer("default_priority").default(2)

    override val primaryKey = PrimaryKey(id)
}

data class IncidentType(
    val id: Int,
    val name: String,
    val icon: String,
    val color: String,
    val defaultPriority: Int
) {
    fun toResponse() = IncidentTypeResponse(
        id              = id,
        name            = name,
        icon            = icon,
        color           = color,
        defaultPriority = defaultPriority
    )
}