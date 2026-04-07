package com.vialreport.backend.repository

import com.vialreport.backend.model.IncidentType
import com.vialreport.backend.model.IncidentTypes
import org.jetbrains.exposed.sql.*

class IncidentTypeRepository {

    private fun rowToType(row: ResultRow) = IncidentType(
        id              = row[IncidentTypes.id],
        name            = row[IncidentTypes.name],
        icon            = row[IncidentTypes.icon],
        color           = row[IncidentTypes.color],
        defaultPriority = row[IncidentTypes.defaultPriority]
    )

    fun findAll(): List<IncidentType> =
        IncidentTypes.selectAll().map { rowToType(it) }

    fun findById(id: Int): IncidentType? =
        IncidentTypes.select { IncidentTypes.id eq id }
            .map { rowToType(it) }.singleOrNull()
}