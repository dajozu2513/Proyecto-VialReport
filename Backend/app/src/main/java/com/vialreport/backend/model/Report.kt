package com.vialreport.backend.model

import org.bson.types.ObjectId
import java.time.LocalDateTime

data class Report(
    val id: ObjectId = ObjectId(),
    val citizenId: String,
    val typeId: String,
    val crewId: String? = null,
    val title: String,
    val description: String,
    val status: String = "new",
    val priority: String = "medium",
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val zone: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
