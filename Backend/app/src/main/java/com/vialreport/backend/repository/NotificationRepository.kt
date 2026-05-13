package com.vialreport.backend.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.vialreport.backend.model.Notification
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

class NotificationRepository(db: MongoDatabase) {

    private val col = db.getCollection<Document>("notifications")

    private fun Date.toLocalDateTime() =
        toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()

    private fun LocalDateTime.toDate() =
        Date.from(atOffset(ZoneOffset.UTC).toInstant())

    private fun docToNotification(doc: Document) = Notification(
        id       = doc.getObjectId("_id"),
        userId   = doc.getString("userId"),
        reportId = doc.getString("reportId"),
        title    = doc.getString("title"),
        body     = doc.getString("body"),
        isRead   = doc.getBoolean("isRead", false),
        sentAt   = (doc.getDate("sentAt") ?: Date()).toLocalDateTime()
    )

    suspend fun findByUser(userId: String): List<Notification> =
        col.find(Filters.eq("userId", userId))
            .sort(Sorts.descending("sentAt"))
            .toList().map { docToNotification(it) }

    suspend fun findUnreadByUser(userId: String): List<Notification> =
        col.find(Filters.and(Filters.eq("userId", userId), Filters.eq("isRead", false)))
            .sort(Sorts.descending("sentAt"))
            .toList().map { docToNotification(it) }

    suspend fun create(userId: String, reportId: String, title: String, body: String): Notification {
        val notif = Notification(userId = userId, reportId = reportId, title = title, body = body)
        val doc = Document("_id", notif.id)
            .append("userId", notif.userId)
            .append("reportId", notif.reportId)
            .append("title", notif.title)
            .append("body", notif.body)
            .append("isRead", notif.isRead)
            .append("sentAt", notif.sentAt.toDate())
        col.insertOne(doc)
        return notif
    }

    suspend fun markAsRead(id: String): Boolean {
        if (!ObjectId.isValid(id)) return false
        return col.updateOne(
            Filters.eq("_id", ObjectId(id)),
            Updates.set("isRead", true)
        ).modifiedCount > 0
    }

    suspend fun markAllAsRead(userId: String): Boolean =
        col.updateMany(
            Filters.and(Filters.eq("userId", userId), Filters.eq("isRead", false)),
            Updates.set("isRead", true)
        ).modifiedCount > 0
}
