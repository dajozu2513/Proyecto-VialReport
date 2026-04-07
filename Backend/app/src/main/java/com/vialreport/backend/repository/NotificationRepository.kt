package com.vialreport.backend.repository

import com.vialreport.backend.model.Notification
import com.vialreport.backend.model.Notifications
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

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

    fun findByUser(userId: Int): List<Notification> {
        return Notifications
            .select { Notifications.userId eq userId }
            .orderBy(Notifications.sentAt, SortOrder.DESC)
            .map { rowToNotification(it) }
    }

    fun findUnreadByUser(userId: Int): List<Notification> {
        return Notifications
            .select { (Notifications.userId eq userId) and (Notifications.isRead eq false) }
            .orderBy(Notifications.sentAt, SortOrder.DESC)
            .map { rowToNotification(it) }
    }

    fun create(
        userId: Int,
        reportId: Int,
        title: String,
        body: String
    ): Notification {
        val id = Notifications.insertAndGetId {
            it[Notifications.userId]   = userId
            it[Notifications.reportId] = reportId
            it[Notifications.title]    = title
            it[Notifications.body]     = body
        }
        return findByUser(userId).first { it.id == id.value }
    }

    fun markAsRead(id: Int): Boolean {
        return Notifications.update({ Notifications.id eq id }) {
            it[Notifications.isRead] = true
        } > 0
    }

    fun markAllAsRead(userId: Int): Boolean {
        return Notifications.update({ Notifications.userId eq userId }) {
            it[Notifications.isRead] = true
        } > 0
    }
}