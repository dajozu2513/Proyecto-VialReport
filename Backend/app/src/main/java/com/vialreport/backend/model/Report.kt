package com.vialreport.backend.model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Reports : IntIdTable("reports") {
    val citizenId   = integer("citizen_id").references(Users.id)
    val typeId      = integer("type_id").references(IncidentTypes.id)
    val crewId      = integer("crew_id").references(Crews.id).nullable()
    val title       = varchar("title", 200)
    val description = text("description")
    val status      = varchar("status", 30).default("new")
    val priority    = varchar("priority", 20).default("medium")
    val latitude    = double("latitude")
    val longitude   = double("longitude")
    val address     = varchar("address", 255)
    val zone        = varchar("zone", 100).nullable()
    val createdAt   = datetime("created_at").default(LocalDateTime.now())
    val updatedAt   = datetime("updated_at").default(LocalDateTime.now())
}

// El data class del reporte guarda los objetos relacionados ya resueltos
data class Report(
    val id: Int,
    val citizenId: Int,
    val typeId: Int,
    val crewId: Int?,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val zone: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)