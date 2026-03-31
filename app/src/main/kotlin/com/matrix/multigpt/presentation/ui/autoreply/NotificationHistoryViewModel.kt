package com.matrix.multigpt.presentation.ui.autoreply

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.multigpt.data.database.entity.NotificationLog
import com.matrix.multigpt.data.database.dao.NotificationLogDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConversationSummary(
    val senderKey: String,
    val senderName: String,
    val appPackage: String,
    val lastMessage: String,
    val lastTime: Long,
    val totalMessages: Int
)

@HiltViewModel
class NotificationHistoryViewModel @Inject constructor(
    private val notificationLogDao: NotificationLogDao
) : ViewModel() {

    // Saare conversations grouped by senderKey
    val conversations: StateFlow<List<ConversationSummary>> =
        notificationLogDao.getAllLogs()
            .map { logs ->
                logs.groupBy { it.senderKey }
                    .map { (senderKey, messages) ->
                        val latest = messages.maxByOrNull { it.createdAt }!!
                        ConversationSummary(
                            senderKey = senderKey,
                            senderName = latest.senderName,
                            appPackage = latest.appPackage,
                            lastMessage = latest.originalMessage,
                            lastTime = latest.createdAt,
                            totalMessages = messages.size
                        )
                    }
                    .sortedByDescending { it.lastTime }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ek sender ke saare messages
    fun getMessagesForSender(senderKey: String): StateFlow<List<NotificationLog>> =
        notificationLogDao.getLogsForSender(senderKey)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearAll() {
        viewModelScope.launch {
            notificationLogDao.deleteAll()
        }
    }

    fun clearSender(senderKey: String) {
        viewModelScope.launch {
            notificationLogDao.deleteForSender(senderKey)
        }
    }
}
