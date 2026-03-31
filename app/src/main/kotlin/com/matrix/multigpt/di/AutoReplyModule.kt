package com.matrix.multigpt.di

import com.matrix.multigpt.data.datastore.AutoReplyDataSource
import com.matrix.multigpt.service.AutoReplyManager
import com.matrix.multigpt.service.ChatMemorySelectorAI
import com.matrix.multigpt.service.NotificationReplyHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AutoReplyModule {

    @Provides
    @Singleton
    fun provideAutoReplyDataSource(
        dataStore: DataStore<Preferences>
    ): AutoReplyDataSource = AutoReplyDataSource(dataStore)

    @Provides
    @Singleton
    fun provideNotificationReplyHelper(): NotificationReplyHelper = NotificationReplyHelper()

    @Provides
    @Singleton
    fun provideChatMemorySelectorAI(
        chatRoomDao: com.matrix.multigpt.data.database.dao.ChatRoomDao,
        messageDao: com.matrix.multigpt.data.database.dao.MessageDao
    ): ChatMemorySelectorAI = ChatMemorySelectorAI(chatRoomDao, messageDao)
}
