package com.vialreport.backend.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.vialreport.backend.dto.AdminStatsResponse
import com.vialreport.backend.dto.HeatmapPoint
import com.vialreport.backend.dto.MapReportPoint
import com.vialreport.backend.model.Report
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

private data class HeatKey(val lat: Double, val lng: Double, val status: String, val typeId: String)

class ReportRepository(db: MongoDatabase) {

    private val col     = db.getCollection<Document>("reports")
    private val typesCol = db.getCollection<Document>("incident_types")

    private fun Date.toLocalDateTime() =
        toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()

    private fun LocalDateTime.toDate() =
        Date.from(atOffset(ZoneOffset.UTC).toInstant())

    private fun docToReport(doc: Document) = Report(
        id          = doc.getObjectId("_id"),
        citizenId   = doc.getString("citizenId"),
        typeId      = doc.getString("typeId"),
        crewId      = doc.getString("crewId"),
        title       = doc.getString("title"),
        description = doc.getString("description"),
        status      = doc.getString("status") ?: "new",
        priority    = doc.getString("priority") ?: "medium",
        latitude    = doc.getDouble("latitude"),
        longitude   = doc.getDouble("longitude"),
        address     = doc.getString("address"),
        zone        = doc.getString("zone"),
        createdAt   = (doc.getDate("createdAt") ?: Date()).toLocalDateTime(),
        updatedAt   = (doc.getDate("updatedAt") ?: Date()).toLocalDateTime()
    )

    suspend fun findById(id: String): Report? {
        if (!ObjectId.isValid(id)) return null
        return col.find(Filters.eq("_id", ObjectId(id))).firstOrNull()?.let { docToReport(it) }
    }

    suspend fun findAll(): List<Report> =
        col.find().sort(Sorts.descending("createdAt")).toList().map { docToReport(it) }

    suspend fun findFiltered(status: String?, typeId: String?, zone: String?): List<Report> =
        col.find(buildFilter(status, typeId, zone))
            .sort(Sorts.descending("createdAt")).toList().map { docToReport(it) }

    private fun buildFilter(status: String?, typeId: String?, zone: String?): org.bson.conversions.Bson {
        val filters = mutableListOf<org.bson.conversions.Bson>()
        if (status != null) filters.add(Filters.eq("status", status))
        if (typeId != null) filters.add(Filters.eq("typeId", typeId))
        if (zone   != null) filters.add(Filters.regex("address", ".*${Regex.escape(zone)}.*", "i"))
        return if (filters.isEmpty()) Filters.empty() else Filters.and(filters)
    }

    suspend fun findByCitizen(citizenId: String): List<Report> =
        col.find(Filters.eq("citizenId", citizenId))
            .sort(Sorts.descending("createdAt")).toList().map { docToReport(it) }

    suspend fun findByStatus(status: String): List<Report> =
        col.find(Filters.eq("status", status))
            .sort(Sorts.descending("createdAt")).toList().map { docToReport(it) }

    suspend fun findByCrew(crewId: String): List<Report> =
        col.find(Filters.eq("crewId", crewId))
            .sort(Sorts.descending("createdAt")).toList().map { docToReport(it) }

    suspend fun create(
        citizenId: String,
        typeId: String,
        title: String,
        description: String,
        priority: String,
        latitude: Double,
        longitude: Double,
        address: String
    ): Report {
        val report = Report(
            citizenId   = citizenId,
            typeId      = typeId,
            title       = title,
            description = description,
            priority    = priority,
            latitude    = latitude,
            longitude   = longitude,
            address     = address
        )
        val doc = Document("_id", report.id)
            .append("citizenId",   report.citizenId)
            .append("typeId",      report.typeId)
            .append("crewId",      report.crewId)
            .append("title",       report.title)
            .append("description", report.description)
            .append("status",      report.status)
            .append("priority",    report.priority)
            .append("latitude",    report.latitude)
            .append("longitude",   report.longitude)
            .append("address",     report.address)
            .append("zone",        report.zone)
            .append("createdAt",   report.createdAt.toDate())
            .append("updatedAt",   report.updatedAt.toDate())
        col.insertOne(doc)
        return report
    }

    suspend fun updateStatus(id: String, status: String, crewId: String?): Report? {
        if (!ObjectId.isValid(id)) return null
        val updates = mutableListOf(
            Updates.set("status", status),
            Updates.set("updatedAt", LocalDateTime.now().toDate())
        )
        if (crewId != null) updates.add(Updates.set("crewId", crewId))
        col.updateOne(Filters.eq("_id", ObjectId(id)), Updates.combine(updates))
        return findById(id)
    }

    suspend fun delete(id: String): Boolean {
        if (!ObjectId.isValid(id)) return false
        return col.deleteOne(Filters.eq("_id", ObjectId(id))).deletedCount > 0
    }

    suspend fun getHeatmapData(typeId: String?, zone: String?, status: String?): List<HeatmapPoint> {
        val filter = buildFilter(status, typeId, zone)
        return col.find(filter).toList()
            .map { doc ->
                HeatKey(
                    lat    = Math.round(doc.getDouble("latitude")  * 1000).toDouble() / 1000.0,
                    lng    = Math.round(doc.getDouble("longitude") * 1000).toDouble() / 1000.0,
                    status = doc.getString("status") ?: "new",
                    typeId = doc.getString("typeId") ?: ""
                )
            }
            .groupingBy { it }.eachCount()
            .map { (key, count) ->
                HeatmapPoint(
                    latitude  = key.lat,
                    longitude = key.lng,
                    weight    = count,
                    status    = key.status,
                    typeId    = key.typeId
                )
            }
    }

    suspend fun getMapPoints(typeId: String?, zone: String?): List<MapReportPoint> {
        val filter = buildFilter(null, typeId, zone)
        val reports = col.find(filter).toList()

        val typeIds = reports.map { it.getString("typeId") }.distinct()
        val typeNames = mutableMapOf<String, String>()
        typeIds.forEach { tid ->
            if (ObjectId.isValid(tid)) {
                typesCol.find(Filters.eq("_id", ObjectId(tid))).firstOrNull()
                    ?.let { typeNames[tid] = it.getString("name") ?: "" }
            }
        }

        return reports.map { doc ->
            MapReportPoint(
                id        = doc.getObjectId("_id").toHexString(),
                latitude  = doc.getDouble("latitude"),
                longitude = doc.getDouble("longitude"),
                status    = doc.getString("status") ?: "new",
                typeName  = typeNames[doc.getString("typeId")] ?: "",
                zone      = doc.getString("zone")
            )
        }
    }

    suspend fun getStats(): AdminStatsResponse {
        val today = LocalDate.now()
        val reports = col.find().toList()

        val typeIds = reports.map { it.getString("typeId") }.distinct()
        val typeNames = mutableMapOf<String, String>()
        typeIds.forEach { tid ->
            if (ObjectId.isValid(tid)) {
                typesCol.find(Filters.eq("_id", ObjectId(tid))).firstOrNull()
                    ?.let { typeNames[tid] = it.getString("name") ?: "Desconocido" }
            }
        }

        data class Row(val status: String, val typeName: String, val zone: String,
                       val createdAt: LocalDateTime, val updatedAt: LocalDateTime)

        val rows = reports.map { doc ->
            Row(
                status    = doc.getString("status") ?: "new",
                typeName  = typeNames[doc.getString("typeId")] ?: "Desconocido",
                zone      = doc.getString("zone") ?: "Sin zona",
                createdAt = (doc.getDate("createdAt") ?: Date()).toLocalDateTime(),
                updatedAt = (doc.getDate("updatedAt") ?: Date()).toLocalDateTime()
            )
        }

        val resolved = rows.filter { it.status == "resolved" }

        return AdminStatsResponse(
            totalReports       = rows.size,
            byStatus           = rows.groupingBy { it.status }.eachCount(),
            byType             = rows.groupingBy { it.typeName }.eachCount(),
            byZone             = rows.groupingBy { it.zone }.eachCount(),
            todayReports       = rows.count { it.createdAt.toLocalDate() == today },
            resolvedToday      = resolved.count { it.updatedAt.toLocalDate() == today },
            avgResolutionHours = if (resolved.isEmpty()) 0.0
                                 else resolved.map {
                                     Duration.between(it.createdAt, it.updatedAt).toHours().toDouble()
                                 }.average()
        )
    }
}
