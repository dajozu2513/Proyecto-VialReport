package com.vialreport.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val id: String,
    val reportId: String,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val sentAt: String
)
