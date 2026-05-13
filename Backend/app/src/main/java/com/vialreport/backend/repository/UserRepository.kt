package com.vialreport.backend.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.vialreport.backend.model.User
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

class UserRepository(db: MongoDatabase) {

    private val col = db.getCollection<Document>("users")

    private fun docToUser(doc: Document) = User(
        id           = doc.getObjectId("_id"),
        name         = doc.getString("name"),
        email        = doc.getString("email"),
        passwordHash = doc.getString("passwordHash"),
        role         = doc.getString("role") ?: "citizen",
        phone        = doc.getString("phone"),
        cedula       = doc.getString("cedula"),
        isVerified   = doc.getBoolean("isVerified", false),
        createdAt    = (doc.getDate("createdAt") ?: Date()).toLocalDateTime()
    )

    private fun Date.toLocalDateTime() =
        toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()

    private fun LocalDateTime.toDate() =
        Date.from(atOffset(ZoneOffset.UTC).toInstant())

    suspend fun findById(id: String): User? {
        if (!ObjectId.isValid(id)) return null
        return col.find(Filters.eq("_id", ObjectId(id))).firstOrNull()?.let { docToUser(it) }
    }

    suspend fun findByEmail(email: String): User? =
        col.find(Filters.eq("email", email)).firstOrNull()?.let { docToUser(it) }

    suspend fun findAll(): List<User> =
        col.find().toList().map { docToUser(it) }

    suspend fun create(
        name: String,
        email: String,
        passwordHash: String,
        role: String,
        phone: String?,
        cedula: String? = null
    ): User {
        val user = User(name = name, email = email, passwordHash = passwordHash,
                        role = role, phone = phone, cedula = cedula)
        val doc = Document("_id", user.id)
            .append("name", user.name)
            .append("email", user.email)
            .append("passwordHash", user.passwordHash)
            .append("role", user.role)
            .append("phone", user.phone)
            .append("cedula", user.cedula)
            .append("isVerified", user.isVerified)
            .append("createdAt", user.createdAt.toDate())
        col.insertOne(doc)
        return user
    }

    suspend fun existsByEmail(email: String): Boolean =
        col.countDocuments(Filters.eq("email", email)) > 0

    suspend fun update(id: String, name: String, phone: String?): User? {
        if (!ObjectId.isValid(id)) return null
        col.updateOne(
            Filters.eq("_id", ObjectId(id)),
            Updates.combine(Updates.set("name", name), Updates.set("phone", phone))
        )
        return findById(id)
    }

    suspend fun delete(id: String): Boolean {
        if (!ObjectId.isValid(id)) return false
        return col.deleteOne(Filters.eq("_id", ObjectId(id))).deletedCount > 0
    }
}
