package com.matrix.multigpt.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.matrix.multigpt.data.database.ChatDatabase
import com.matrix.multigpt.data.database.dao.ChatRoomDao
import com.matrix.multigpt.data.database.dao.MessageDao
import com.matrix.multigpt.data.database.dao.NotificationLogDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DB_NAME = "chat"

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE messages ADD COLUMN model_name TEXT")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS notification_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    senderName TEXT NOT NULL,
                    senderKey TEXT NOT NULL,
                    appPackage TEXT NOT NULL,
                    originalMessage TEXT NOT NULL,
                    aiReply TEXT NOT NULL DEFAULT '',
                    status TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
            """.trimIndent())
        }
    }

    @Provides
    fun provideChatRoomDao(chatDatabase: ChatDatabase): ChatRoomDao = chatDatabase.chatRoomDao()

    @Provides
    fun provideMessageDao(chatDatabase: ChatDatabase): MessageDao = chatDatabase.messageDao()

    @Provides
    fun provideNotificationLogDao(chatDatabase: ChatDatabase): NotificationLogDao = chatDatabase.notificationLogDao()

    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext appContext: Context): ChatDatabase =
        Room.databaseBuilder(
            appContext,
            ChatDatabase::class.java,
            DB_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
}
