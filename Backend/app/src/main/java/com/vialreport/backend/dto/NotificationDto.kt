package com.vialreport.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val id: Int,
    val reportId: Int,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val sentAt: String
)