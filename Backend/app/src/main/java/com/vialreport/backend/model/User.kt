package com.vialreport.backend.model

import com.vialreport.backend.dto.UserResponse
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class User(
    val id: ObjectId = ObjectId(),
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String = "citizen",
    val phone: String? = null,
    val cedula: String? = null,
    val isVerified: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toResponse() = UserResponse(
        id         = id.toHexString(),
        name       = name,
        email      = email,
        role       = role,
        phone      = phone,
        isVerified = isVerified,
        createdAt  = createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}
