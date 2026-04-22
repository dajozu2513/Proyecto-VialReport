package com.vialreport.backend.repository

import com.vialreport.backend.model.Crew
import com.vialreport.backend.model.Crews
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class CrewRepository {

    private fun rowToCrew(row: ResultRow) = Crew(
        id        = row[Crews.id].value,
        name      = row[Crews.name],
        zone      = row[Crews.zone],
        available = row[Crews.available]
    )

    fun findAll(): List<Crew> = transaction {
        Crews.selectAll().map { rowToCrew(it) }
    }

    fun findById(id: Int): Crew? = transaction {
        Crews.select { Crews.id eq id }.map { rowToCrew(it) }.singleOrNull()
    }

    fun findAvailable(): List<Crew> = transaction {
        Crews.select { Crews.available eq true }.map { rowToCrew(it) }
    }

    fun create(name: String, zone: String): Crew = transaction {
        val id = Crews.insertAndGetId {
            it[Crews.name] = name
            it[Crews.zone] = zone
        }
        Crews.select { Crews.id eq id }.map { rowToCrew(it) }.single()
    }

    fun updateAvailability(id: Int, available: Boolean): Crew? = transaction {
        Crews.update({ Crews.id eq id }) { it[Crews.available] = available }
        Crews.select { Crews.id eq id }.map { rowToCrew(it) }.singleOrNull()
    }

    fun delete(id: Int): Boolean = transaction {
        Crews.deleteWhere { Crews.id eq id } > 0
    }
}
