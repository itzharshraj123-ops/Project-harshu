package com.matrix.multigpt.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.matrix.multigpt.data.database.entity.NotificationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLog)

    @Query("SELECT * FROM notification_logs ORDER BY createdAt DESC")
    fun getAllLogs(): Flow<List<NotificationLog>>

    @Query("SELECT * FROM notification_logs WHERE senderKey = :senderKey ORDER BY createdAt ASC")
    fun getLogsForSender(senderKey: String): Flow<List<NotificationLog>>

    @Query("DELETE FROM notification_logs")
    suspend fun deleteAll()

    @Query("DELETE FROM notification_logs WHERE senderKey = :senderKey")
    suspend fun deleteForSender(senderKey: String)
}
