package com.vialreport.backend.util

object UserRole {
    const val CITIZEN = "citizen"
    const val ADMIN = "admin"
    const val CREW_MEMBER = "crew_member"
}

object ReportStatus {
    const val NEW = "new"
    const val VERIFIED = "verified"
    const val IN_PROGRESS = "in_progress"
    const val REPAIRING = "repairing"
    const val RESOLVED = "resolved"
    const val REJECTED = "rejected"
    const val DUPLICATE = "duplicate"

    // Valida que el status sea uno de los permitidos
    fun isValid(status: String): Boolean {
        return status in listOf(
            NEW, VERIFIED, IN_PROGRESS,
            REPAIRING, RESOLVED, REJECTED, DUPLICATE
        )
    }

    // Solo admins pueden asignar estos estados
    fun requiresAdmin(status: String): Boolean {
        return status in listOf(VERIFIED, IN_PROGRESS, REPAIRING, REJECTED, DUPLICATE)
    }
}

object ReportPriority {
    const val LOW = "low"
    const val MEDIUM = "medium"
    const val HIGH = "high"
    const val CRITICAL = "critical"

    fun isValid(priority: String): Boolean {
        return priority in listOf(LOW, MEDIUM, HIGH, CRITICAL)
    }
}