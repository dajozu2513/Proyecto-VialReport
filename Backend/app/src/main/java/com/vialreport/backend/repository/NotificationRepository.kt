package com.vialreport.backend.repository

import com.vialreport.backend.model.Notification
import com.vialreport.backend.model.Notifications
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class NotificationRepository {

    private fun rowToNotification(row: ResultRow) = Notification(
        id       = row[Notifications.id].value,
        userId   = row[Notifications.userId],
        reportId = row[Notifications.reportId],
        title    = row[Notifications.title],
        body     = row[Notifications.body],
        isRead   = row[Notifications.isRead],
        sentAt   = row[Notifications.sentAt]
    )

    fun findByUser(userId: Int): List<Notification> = transaction {
        Notifications.select { Notifications.userId eq userId }
            .orderBy(Notifications.sentAt, SortOrder.DESC)
            .map { rowToNotification(it) }
    }

    fun findUnreadByUser(userId: Int): List<Notification> = transaction {
        Notifications
            .select { (Notifications.userId eq userId) and (Notifications.isRead eq false) }
            .orderBy(Notifications.sentAt, SortOrder.DESC)
            .map { rowToNotification(it) }
    }

    fun create(userId: Int, reportId: Int, title: String, body: String): Notification = transaction {
        val id = Notifications.insertAndGetId {
            it[Notifications.userId]   = userId
            it[Notifications.reportId] = reportId
            it[Notifications.title]    = title
            it[Notifications.body]     = body
        }
        Notifications.select { Notifications.id eq id }.map { rowToNotification(it) }.single()
    }

    fun markAsRead(id: Int): Boolean = transaction {
        Notifications.update({ Notifications.id eq id }) {
            it[Notifications.isRead] = true
        } > 0
    }

    fun markAllAsRead(userId: Int): Boolean = transaction {
        Notifications.update({ Notifications.userId eq userId }) {
            it[Notifications.isRead] = true
        } > 0
    }
}
