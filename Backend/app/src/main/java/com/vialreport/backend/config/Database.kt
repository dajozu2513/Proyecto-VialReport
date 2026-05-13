package com.vialreport.backend.config

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import org.bson.Document

object DatabaseFactory {

    lateinit var db: MongoDatabase
        private set

    fun init(environment: ApplicationEnvironment) {
        val uri = environment.config.property("mongodb.uri").getString()
        val client = MongoClient.create(uri)
        db = client.getDatabase("vialreport")

        runBlocking { seedIncidentTypes() }
    }

    private suspend fun seedIncidentTypes() {
        val collection = db.getCollection<Document>("incident_types")
        if (collection.countDocuments() > 0L) return

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

        val docs = types.map { (name, icon, color) ->
            Document("name", name)
                .append("icon", icon)
                .append("color", color)
                .append("defaultPriority", 2)
        }
        collection.insertMany(docs)
    }
}
