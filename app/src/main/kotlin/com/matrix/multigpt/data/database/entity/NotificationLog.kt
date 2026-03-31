package com.matrix.multigpt.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val senderName: String,
    val originalMessage: String,
    val aiReply: String,
    val appPackage: String,
    val status: String, // "success", "failed", "received"
    val createdAt: Long = System.currentTimeMillis()
)
