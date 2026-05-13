package com.vialreport.backend.repository

import com.vialreport.backend.dto.AdminStatsResponse
import com.vialreport.backend.dto.HeatmapPoint
import com.vialreport.backend.dto.MapReportPoint
import com.vialreport.backend.model.IncidentTypes
import com.vialreport.backend.model.Report
import com.vialreport.backend.model.Reports
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

private data class HeatKey(val lat: Double, val lng: Double, val status: String, val typeId: Int)
private data class StatsRow(
    val status:    String,
    val typeName:  String,
    val zone:      String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

class ReportRepository {

    private fun rowToReport(row: ResultRow) = Report(
        id          = row[Reports.id].value,
        citizenId   = row[Reports.citizenId],
        typeId      = row[Reports.typeId],
        crewId      = row[Reports.crewId],
        title       = row[Reports.title],
        description = row[Reports.description],
        status      = row[Reports.status],
        priority    = row[Reports.priority],
        latitude    = row[Reports.latitude],
        longitude   = row[Reports.longitude],
        address     = row[Reports.address],
        zone        = row[Reports.zone],
        createdAt   = row[Reports.createdAt],
        updatedAt   = row[Reports.updatedAt]
    )

    fun findById(id: Int): Report? = transaction {
        Reports.select { Reports.id eq id }.map { rowToReport(it) }.singleOrNull()
    }

    fun findAll(): List<Report> = transaction {
        Reports.selectAll().orderBy(Reports.createdAt, SortOrder.DESC).map { rowToReport(it) }
    }

    fun findFiltered(status: String?, typeId: Int?, zone: String?): List<Report> = transaction {
        Reports.selectAll().apply {
            if (status != null) andWhere { Reports.status eq status }
            if (typeId != null) andWhere { Reports.typeId eq typeId }
            // zone filtra por address (LIKE) ya que zone es nullable y no soporta like directamente
            if (zone   != null) andWhere { Reports.address like "%$zone%" }
        }.orderBy(Reports.createdAt, SortOrder.DESC).map { rowToReport(it) }
    }

    fun findByCitizen(citizenId: Int): List<Report> = transaction {
        Reports.select { Reports.citizenId eq citizenId }
            .orderBy(Reports.createdAt, SortOrder.DESC).map { rowToReport(it) }
    }

    fun findByStatus(status: String): List<Report> = transaction {
        Reports.select { Reports.status eq status }
            .orderBy(Reports.createdAt, SortOrder.DESC).map { rowToReport(it) }
    }

    fun findByCrew(crewId: Int): List<Report> = transaction {
        Reports.select { Reports.crewId eq crewId }
            .orderBy(Reports.createdAt, SortOrder.DESC).map { rowToReport(it) }
    }

    fun create(
        citizenId: Int,
        typeId: Int,
        title: String,
        description: String,
        priority: String,
        latitude: Double,
        longitude: Double,
        address: String
    ): Report = transaction {
        val id = Reports.insertAndGetId {
            it[Reports.citizenId]   = citizenId
            it[Reports.typeId]      = typeId
            it[Reports.title]       = title
            it[Reports.description] = description
            it[Reports.priority]    = priority
            it[Reports.latitude]    = latitude
            it[Reports.longitude]   = longitude
            it[Reports.address]     = address
        }
        Reports.select { Reports.id eq id }.map { rowToReport(it) }.single()
    }

    fun updateStatus(id: Int, status: String, crewId: Int?): Report? = transaction {
        Reports.update({ Reports.id eq id }) {
            it[Reports.status]    = status
            it[Reports.updatedAt] = LocalDateTime.now()
            if (crewId != null) it[Reports.crewId] = crewId
        }
        Reports.select { Reports.id eq id }.map { rowToReport(it) }.singleOrNull()
    }

    fun delete(id: Int): Boolean = transaction {
        Reports.deleteWhere { Reports.id eq id } > 0
    }

    fun getHeatmapData(typeId: Int?, zone: String?, status: String?): List<HeatmapPoint> = transaction {
        Reports.selectAll().apply {
            andWhere { Reports.status notInList listOf("rejected", "duplicate") }
            if (status != null) andWhere { Reports.status eq status }
            if (typeId != null) andWhere { Reports.typeId eq typeId }
            if (zone   != null) andWhere { Reports.address like "%$zone%" }
        }.map { row ->
            HeatKey(
                lat    = Math.round(row[Reports.latitude]  * 1000).toDouble() / 1000.0,
                lng    = Math.round(row[Reports.longitude] * 1000).toDouble() / 1000.0,
                status = row[Reports.status],
                typeId = row[Reports.typeId]
            )
        }.groupingBy { it }.eachCount().map { (key, count) ->
            HeatmapPoint(
                latitude  = key.lat,
                longitude = key.lng,
                weight    = count,
                status    = key.status,
                typeId    = key.typeId
            )
        }
    }

    fun getMapPoints(typeId: Int?, zone: String?): List<MapReportPoint> = transaction {
        Reports.join(IncidentTypes, JoinType.INNER, Reports.typeId, IncidentTypes.id)
            .selectAll().apply {
                if (typeId != null) andWhere { Reports.typeId eq typeId }
                if (zone   != null) andWhere { Reports.address like "%$zone%" }
            }.map { row ->
                MapReportPoint(
                    id        = row[Reports.id].value,
                    latitude  = row[Reports.latitude],
                    longitude = row[Reports.longitude],
                    status    = row[Reports.status],
                    typeName  = row[IncidentTypes.name],
                    zone      = row[Reports.zone]
                )
            }
    }

    fun getStats(): AdminStatsResponse = transaction {
        val today = LocalDate.now()

        val rows = Reports.join(IncidentTypes, JoinType.INNER, Reports.typeId, IncidentTypes.id)
            .selectAll()
            .map { row ->
                StatsRow(
                    status    = row[Reports.status],
                    typeName  = row[IncidentTypes.name],
                    zone      = row[Reports.zone] ?: "Sin zona",
                    createdAt = row[Reports.createdAt],
                    updatedAt = row[Reports.updatedAt]
                )
            }

        val resolved = rows.filter { it.status == "resolved" }

        AdminStatsResponse(
            totalReports       = rows.size,
            byStatus           = rows.groupingBy { it.status }.eachCount(),
            byType             = rows.groupingBy { it.typeName }.eachCount(),
            byZone             = rows.groupingBy { it.zone }.eachCount(),
            todayReports       = rows.count { it.createdAt.toLocalDate() == today },
            resolvedToday      = resolved.count { it.updatedAt.toLocalDate() == today },
            avgResolutionHours = if (resolved.isEmpty()) 0.0
                                 else resolved.map { Duration.between(it.createdAt, it.updatedAt).toHours().toDouble() }.average()
        )
    }
}
