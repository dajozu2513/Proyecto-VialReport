package com.vialreport.backend.repository

import com.vialreport.backend.model.Crew
import com.vialreport.backend.model.Crews
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class CrewRepository {

    private fun rowToCrew(row: ResultRow) = Crew(
        id        = row[Crews.id].value,
        name      = row[Crews.name],
        zone      = row[Crews.zone],
        available = row[Crews.available]
    )

    fun findAll(): List<Crew> = Crews.selectAll().map { rowToCrew(it) }

    fun findById(id: Int): Crew? =
        Crews.select { Crews.id eq id }.map { rowToCrew(it) }.singleOrNull()

    fun findAvailable(): List<Crew> =
        Crews.select { Crews.available eq true }.map { rowToCrew(it) }

    fun create(name: String, zone: String): Crew {
        val id = Crews.insertAndGetId {
            it[Crews.name] = name
            it[Crews.zone] = zone
        }
        return findById(id.value)!!
    }

    fun updateAvailability(id: Int, available: Boolean): Crew? {
        Crews.update({ Crews.id eq id }) { it[Crews.available] = available }
        return findById(id)
    }

    fun delete(id: Int): Boolean = Crews.deleteWhere { Crews.id eq id } > 0
}