package com.matrix.multigpt.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.matrix.multigpt.data.database.dao.ChatRoomDao
import com.matrix.multigpt.data.database.dao.MessageDao
import com.matrix.multigpt.data.database.dao.NotificationLogDao
import com.matrix.multigpt.data.database.entity.APITypeConverter
import com.matrix.multigpt.data.database.entity.ChatRoom
import com.matrix.multigpt.data.database.entity.Message
import com.matrix.multigpt.data.database.entity.NotificationLog

@Database(
    entities = [ChatRoom::class, Message::class, NotificationLog::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(APITypeConverter::class)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun messageDao(): MessageDao
    abstract fun notificationLogDao(): NotificationLogDao
}
