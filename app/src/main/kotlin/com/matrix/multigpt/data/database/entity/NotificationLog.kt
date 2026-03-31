package com.matrix.multigpt.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val senderName: String,
    val senderKey: String, // phone number ya unique identifier
    val appPackage: String,
    val originalMessage: String,
    val aiReply: String = "",
    val status: String, // "received", "success", "failed"
    val createdAt: Long = System.currentTimeMillis()
)
