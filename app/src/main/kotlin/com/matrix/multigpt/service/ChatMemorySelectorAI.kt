package com.matrix.multigpt.service

import com.matrix.multigpt.data.database.dao.ChatRoomDao
import com.matrix.multigpt.data.database.dao.MessageDao
import com.matrix.multigpt.data.database.entity.ChatRoom
import com.matrix.multigpt.data.database.entity.Message
import javax.inject.Inject

class ChatMemorySelectorAI @Inject constructor(
    private val chatRoomDao: ChatRoomDao,
    private val messageDao: MessageDao
) {

    // Sabse relevant chat room dhundega incoming message ke basis pe
    suspend fun selectRelevantChat(incomingMessage: String, senderName: String): Pair<ChatRoom?, List<Message>> {
        val allChats = chatRoomDao.getChatRooms()

        if (allChats.isEmpty()) return Pair(null, emptyList())

        // Step 1: Sender name se match karo chat title mein
        val nameMatchedChat = allChats.firstOrNull { chat ->
            chat.title?.contains(senderName, ignoreCase = true) == true
        }

        if (nameMatchedChat != null) {
            val messages = messageDao.loadMessages(nameMatchedChat.id)
            return Pair(nameMatchedChat, messages)
        }

        // Step 2: Keywords se relevant chat dhundo
        val incomingWords = incomingMessage
            .lowercase()
            .split(" ", ",", ".", "?", "!")
            .filter { it.length > 3 }

        var bestChat: ChatRoom? = null
        var bestScore = 0

        for (chat in allChats) {
            val messages = messageDao.loadMessages(chat.id)
            val chatText = messages.joinToString(" ") { it.content }.lowercase()

            val score = incomingWords.count { word -> chatText.contains(word) }

            if (score > bestScore) {
                bestScore = score
                bestChat = chat
            }
        }

        // Step 3: Agar koi match nahi toh latest chat use karo
        if (bestChat == null || bestScore == 0) {
            val latestChat = allChats.maxByOrNull { it.id }
            val messages = latestChat?.let { messageDao.loadMessages(it.id) } ?: emptyList()
            return Pair(latestChat, messages)
        }

        val bestMessages = messageDao.loadMessages(bestChat.id)
        return Pair(bestChat, bestMessages)
    }

    // Context string banao AI ko dene ke liye - last 10 messages tak
    fun buildContextFromMessages(messages: List<Message>, chatRoom: ChatRoom?): String {
        if (messages.isEmpty()) return ""

        val recentMessages = messages.takeLast(10)
        val sb = StringBuilder()

        chatRoom?.title?.let {
            sb.appendLine("Chat context: $it")
            sb.appendLine("---")
        }

        recentMessages.forEach { msg ->
            val role = if (msg.platformType == null) "User" else "AI"
            sb.appendLine("$role: ${msg.content}")
        }

        return sb.toString()
    }
}
