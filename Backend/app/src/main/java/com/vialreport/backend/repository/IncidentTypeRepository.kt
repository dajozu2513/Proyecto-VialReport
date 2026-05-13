package com.vialreport.backend.repository

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.vialreport.backend.model.IncidentType
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId

class IncidentTypeRepository(db: MongoDatabase) {

    private val col = db.getCollection<Document>("incident_types")

    private fun docToType(doc: Document) = IncidentType(
        id              = doc.getObjectId("_id"),
        name            = doc.getString("name"),
        icon            = doc.getString("icon"),
        color           = doc.getString("color"),
        defaultPriority = doc.getInteger("defaultPriority", 2)
    )

    suspend fun findAll(): List<IncidentType> =
        col.find().toList().map { docToType(it) }

    suspend fun findById(id: String): IncidentType? {
        if (!ObjectId.isValid(id)) return null
        return col.find(Filters.eq("_id", ObjectId(id))).firstOrNull()?.let { docToType(it) }
    }
}
