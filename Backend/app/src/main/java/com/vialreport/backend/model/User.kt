package com.vialreport.backend.model

import com.vialreport.backend.dto.UserResponse
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ── Tabla ──────────────────────────────────────────────────────
object Users : IntIdTable("users") {
    val name         = varchar("name", 100)
    val email        = varchar("email", 150).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role         = varchar("role", 20).default("citizen")
    val phone        = varchar("phone", 20).nullable()
    val cedula       = varchar("cedula", 20).nullable()
    val isVerified   = bool("is_verified").default(false)
    val createdAt    = datetime("created_at").default(LocalDateTime.now())
}
// ── Data class ─────────────────────────────────────────────────
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String,
    val phone: String?,
    val cedula: String?,
    val isVerified: Boolean,
    val createdAt: LocalDateTime
) {
    fun toResponse() = UserResponse(
        id         = id,
        name       = name,
        email      = email,
        role       = role,
        phone      = phone,
        isVerified = isVerified,
        createdAt  = createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}