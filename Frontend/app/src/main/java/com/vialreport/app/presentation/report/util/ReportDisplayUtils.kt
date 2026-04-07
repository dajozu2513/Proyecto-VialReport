package com.vialreport.app.presentation.report.util

import androidx.compose.ui.graphics.Color

fun statusLabel(status: String): String = when (status) {
    "new" -> "Nuevo"
    "verified" -> "Verificado"
    "in_progress" -> "En progreso"
    "repairing" -> "En reparación"
    "resolved" -> "Resuelto"
    "rejected" -> "Rechazado"
    "duplicate" -> "Duplicado"
    else -> status
}

fun statusColor(status: String): Color = when (status) {
    "new" -> Color(0xFF1565C0)
    "verified" -> Color(0xFF6A1B9A)
    "in_progress" -> Color(0xFFE65100)
    "repairing" -> Color(0xFFF9A825)
    "resolved" -> Color(0xFF2E7D32)
    "rejected" -> Color(0xFFC62828)
    "duplicate" -> Color(0xFF546E7A)
    else -> Color.Gray
}

fun typeLabel(type: String): String = when (type) {
    "pothole" -> "Bache"
    "damaged_signal" -> "Señal dañada"
    "street_lighting" -> "Alumbrado"
    "flooding" -> "Inundación"
    "road_debris" -> "Derrumbe"
    else -> type
}

fun priorityLabel(priority: String): String = when (priority) {
    "low" -> "Baja"
    "medium" -> "Media"
    "high" -> "Alta"
    "critical" -> "Crítica"
    else -> priority
}

fun priorityColor(priority: String): Color = when (priority) {
    "low" -> Color(0xFF388E3C)
    "medium" -> Color(0xFFF57C00)
    "high" -> Color(0xFFD32F2F)
    "critical" -> Color(0xFF880E4F)
    else -> Color.Gray
}
