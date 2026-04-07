package com.vialreport.backend.config

import com.vialreport.backend.model.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(environment: ApplicationEnvironment) {
        val config = environment.config
        val url = config.property("database.url").getString()
        val user = config.property("database.user").getString()
        val password = config.property("database.password").getString()
        val driver = config.property("database.driver").getString()

        // ── HikariCP — pool de conexiones ─────────────────────
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = url
            username = user
            this.password = password
            driverClassName = driver
            maximumPoolSize = 10
            minimumIdle = 2
            idleTimeout = 600_000
            connectionTimeout = 30_000
        }

        Database.connect(HikariDataSource(hikariConfig))

        // ── Crear tablas si no existen ────────────────────────
        transaction {
            SchemaUtils.create(
                Users,
                IncidentTypes,
                Crews,
                Reports,
                ReportPhotos,
                ReportStatusLogs,
                Notifications
            )

            // Insertar tipos de incidente por defecto si la tabla está vacía
            seedIncidentTypes()
        }
    }

    private fun seedIncidentTypes() {
        if (IncidentTypes.selectAll().count() > 0L) return

        val types = listOf(
            Triple("Bache", "🕳️", "#ef4444"),
            Triple("Señal dañada", "🚧", "#f97316"),
            Triple("Alumbrado público", "💡", "#eab308"),
            Triple("Grieta en acera", "⚠️", "#f97316"),
            Triple("Semáforo dañado", "🚦", "#ef4444"),
            Triple("Derrumbe", "🪨", "#dc2626"),
            Triple("Inundación", "🌊", "#3b82f6"),
            Triple("Basura acumulada", "🗑️", "#22c55e")
        )

        types.forEach { (name, icon, color) ->
            IncidentTypes.insert {
                it[IncidentTypes.name] = name
                it[IncidentTypes.icon] = icon
                it[IncidentTypes.color] = color
                it[IncidentTypes.defaultPriority] = 2
            }
        }
    }
}