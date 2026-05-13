package com.vialreport.backend.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.vialreport.backend.model.Crew
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId

class CrewRepository(db: MongoDatabase) {

    private val col = db.getCollection<Document>("crews")

    private fun docToCrew(doc: Document) = Crew(
        id        = doc.getObjectId("_id"),
        name      = doc.getString("name"),
        zone      = doc.getString("zone"),
        available = doc.getBoolean("available", true)
    )

    suspend fun findAll(): List<Crew> =
        col.find().toList().map { docToCrew(it) }

    suspend fun findById(id: String): Crew? {
        if (!ObjectId.isValid(id)) return null
        return col.find(Filters.eq("_id", ObjectId(id))).firstOrNull()?.let { docToCrew(it) }
    }

    suspend fun findAvailable(): List<Crew> =
        col.find(Filters.eq("available", true)).toList().map { docToCrew(it) }

    suspend fun create(name: String, zone: String): Crew {
        val crew = Crew(name = name, zone = zone)
        val doc = Document("_id", crew.id)
            .append("name", crew.name)
            .append("zone", crew.zone)
            .append("available", crew.available)
        col.insertOne(doc)
        return crew
    }

    suspend fun updateAvailability(id: String, available: Boolean): Crew? {
        if (!ObjectId.isValid(id)) return null
        col.updateOne(Filters.eq("_id", ObjectId(id)), Updates.set("available", available))
        return findById(id)
    }

    suspend fun delete(id: String): Boolean {
        if (!ObjectId.isValid(id)) return false
        return col.deleteOne(Filters.eq("_id", ObjectId(id))).deletedCount > 0
    }
}
