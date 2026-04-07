package com.vialreport.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class CrewRequest(
    val name: String,
    val zone: String
)

@Serializable
data class CrewResponse(
    val id: Int,
    val name: String,
    val zone: String,
    val available: Boolean
)

@Serializable
data class IncidentTypeResponse(
    val id: Int,
    val name: String,
    val icon: String,
    val color: String,
    val defaultPriority: Int
)