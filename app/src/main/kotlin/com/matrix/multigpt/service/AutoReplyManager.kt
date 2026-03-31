package com.matrix.multigpt.service

import android.service.notification.StatusBarNotification
import com.matrix.multigpt.data.datastore.AutoReplyDataSource
import com.matrix.multigpt.data.repository.ChatRepository
import com.matrix.multigpt.data.repository.SettingRepository
import com.matrix.multigpt.data.database.entity.Message
import com.matrix.multigpt.data.model.ApiType
import com.matrix.multigpt.data.dto.ApiState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

@Singleton
class AutoReplyManager @Inject constructor(
    private val autoReplyDataSource: AutoReplyDataSource,
    private val chatMemorySelectorAI: ChatMemorySelectorAI,
    private val notificationReplyHelper: NotificationReplyHelper,
    private val chatRepository: ChatRepository,
    private val settingRepository: SettingRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Processing mein hai kaunse notifications - duplicate reply rokne ke liye
    private val processingKeys = mutableSetOf<String>()

    fun handleNotification(sbn: StatusBarNotification) {
        val key = "${sbn.packageName}_${sbn.id}_${sbn.tag}"

        // Duplicate check
        if (processingKeys.contains(key)) return

        scope.launch {
            try {
                // Master switch check
                val masterOn = autoReplyDataSource.masterEnabled.first()
                if (!masterOn) return@launch

                // App-wise check
                val appEnabled = when (sbn.packageName) {
                    NotificationReplyHelper.WHATSAPP_PACKAGE,
                    NotificationReplyHelper.WHATSAPP_BUSINESS_PACKAGE ->
                        autoReplyDataSource.whatsappEnabled.first()

                    NotificationReplyHelper.TELEGRAM_PACKAGE,
                    NotificationReplyHelper.TELEGRAM_X_PACKAGE ->
                        autoReplyDataSource.telegramEnabled.first()

                    NotificationReplyHelper.INSTAGRAM_PACKAGE ->
                        autoReplyDataSource.instagramEnabled.first()

                    else -> false
                }
                if (!appEnabled) return@launch

                // Group notification ignore karo
                if (notificationReplyHelper.isGroupNotification(sbn)) return@launch

                val senderName = notificationReplyHelper.extractSenderName(sbn)
                val messageText = notificationReplyHelper.extractMessageText(sbn)

                if (messageText.isBlank()) return@launch

                processingKeys.add(key)

                // Delay - natural feel ke liye
                val delay = autoReplyDataSource.replyDelaySeconds.first()
                delay(delay * 1000L)

                // AI se reply generate karo
                val replyText = generateReply(senderName, messageText)

                if (replyText.isNotBlank()) {
                    notificationReplyHelper.sendReply(sbn, replyText)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                processingKeys.remove(key)
            }
        }
    }

    private suspend fun generateReply(senderName: String, incomingMessage: String): String {
        // Step 1: Relevant chat memory select karo
        val (chatRoom, history) = chatMemorySelectorAI.selectRelevantChat(incomingMessage, senderName)
        val context = chatMemorySelectorAI.buildContextFromMessages(history, chatRoom)

        // Step 2: Question message banao
        val fullPrompt = buildString {
            if (context.isNotBlank()) {
                appendLine("Previous conversation context:")
                appendLine(context)
                appendLine("---")
            }
            appendLine("$senderName ne message kiya: $incomingMessage")
            appendLine("Iska ek chhota natural reply do. Sirf reply text do, kuch extra mat likho.")
        }

        val questionMessage = Message(
            id = 0,
            chatId = 0,
            content = fullPrompt,
            platformType = null,
            createdAt = System.currentTimeMillis()
        )

        // Step 3: Active platform dhundo aur reply lo
        return try {
            val platforms = settingRepository.fetchPlatforms()
            val activePlatform = platforms.firstOrNull { it.isEnabled } ?: return ""

            val replyFlow = when (activePlatform.name) {
                ApiType.OPENAI -> chatRepository.completeOpenAIChat(questionMessage, history)
                ApiType.ANTHROPIC -> chatRepository.completeAnthropicChat(questionMessage, history)
                ApiType.GOOGLE -> chatRepository.completeGoogleChat(questionMessage, history)
                ApiType.GROQ -> chatRepository.completeGroqChat(questionMessage, history)
                ApiType.OLLAMA -> chatRepository.completeOllamaChat(questionMessage, history)
                ApiType.BEDROCK -> chatRepository.completeBedrockChat(questionMessage, history)
                else -> return ""
            }

            val sb = StringBuilder()
            replyFlow.collect { state ->
                when (state) {
                    is ApiState.Success -> sb.append(state.data)
                    is ApiState.Error -> return ""
                    else -> {}
                }
            }
            sb.toString().trim()

        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
