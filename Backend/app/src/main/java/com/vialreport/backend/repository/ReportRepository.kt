package com.vialreport.backend.repository

import com.vialreport.backend.model.Report
import com.vialreport.backend.model.Reports
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

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
        createdAt   = row[Reports.createdAt],
        updatedAt   = row[Reports.updatedAt]
    )

    fun findById(id: Int): Report? {
        return Reports
            .select { Reports.id eq id }
            .map { rowToReport(it) }
            .singleOrNull()
    }

    fun findAll(): List<Report> {
        return Reports
            .selectAll()
            .orderBy(Reports.createdAt, SortOrder.DESC)
            .map { rowToReport(it) }
    }

    fun findByCitizen(citizenId: Int): List<Report> {
        return Reports
            .select { Reports.citizenId eq citizenId }
            .orderBy(Reports.createdAt, SortOrder.DESC)
            .map { rowToReport(it) }
    }

    fun findByStatus(status: String): List<Report> {
        return Reports
            .select { Reports.status eq status }
            .orderBy(Reports.createdAt, SortOrder.DESC)
            .map { rowToReport(it) }
    }

    fun findByCrew(crewId: Int): List<Report> {
        return Reports
            .select { Reports.crewId eq crewId }
            .orderBy(Reports.createdAt, SortOrder.DESC)
            .map { rowToReport(it) }
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
    ): Report {
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
        return findById(id.value)!!
    }

    fun updateStatus(id: Int, status: String, crewId: Int?): Report? {
        Reports.update({ Reports.id eq id }) {
            it[Reports.status]    = status
            it[Reports.updatedAt] = LocalDateTime.now()
            if (crewId != null) it[Reports.crewId] = crewId
        }
        return findById(id)
    }

    fun delete(id: Int): Boolean {
        return Reports
            .deleteWhere { Reports.id eq id } > 0
    }
}