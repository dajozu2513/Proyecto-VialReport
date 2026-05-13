package com.vialreport.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class IncidentTypeResponse(
    val id: Int,
    val name: String,
    val icon: String,
    val color: String,
    val defaultPriority: Int
)
