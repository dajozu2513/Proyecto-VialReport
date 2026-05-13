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

