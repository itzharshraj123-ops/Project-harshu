package com.matrix.multigpt.service

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import javax.inject.Inject

class NotificationReplyHelper @Inject constructor() {

    companion object {
        const val WHATSAPP_PACKAGE = "com.whatsapp"
        const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
        const val TELEGRAM_PACKAGE = "org.telegram.messenger"
        const val TELEGRAM_X_PACKAGE = "org.thunderdog.challegram"
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
    }

    // Notification se sender name nikalo
    fun extractSenderName(sbn: StatusBarNotification): String {
        val extras = sbn.notification.extras
        return when (sbn.packageName) {
            WHATSAPP_PACKAGE, WHATSAPP_BUSINESS_PACKAGE -> {
                extras.getString(Notification.EXTRA_TITLE) ?: "Unknown"
            }
            TELEGRAM_PACKAGE, TELEGRAM_X_PACKAGE -> {
                extras.getString(Notification.EXTRA_TITLE) ?: "Unknown"
            }
            INSTAGRAM_PACKAGE -> {
                extras.getString(Notification.EXTRA_TITLE) ?: "Unknown"
            }
            else -> extras.getString(Notification.EXTRA_TITLE) ?: "Unknown"
        }
    }

    // Notification se message text nikalo
    fun extractMessageText(sbn: StatusBarNotification): String {
        val extras = sbn.notification.extras
        return extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""
    }

    // Reply action dhundo notification mein
    fun findReplyAction(sbn: StatusBarNotification): Notification.Action? {
        val actions = sbn.notification.actions ?: return null

        return actions.firstOrNull { action ->
            val label = action.title?.toString()?.lowercase() ?: ""
            val hasRemoteInput = action.remoteInputs?.isNotEmpty() == true
            hasRemoteInput && (
                label.contains("reply") ||
                label.contains("jawab") ||
                label.contains("respond") ||
                label == "r" ||
                label.isEmpty()
            )
        } ?: actions.firstOrNull { action ->
            // Fallback - koi bhi action jo RemoteInput support kare
            action.remoteInputs?.isNotEmpty() == true
        }
    }

    // Reply bhejo notification action se
    fun sendReply(sbn: StatusBarNotification, replyText: String): Boolean {
        return try {
            val replyAction = findReplyAction(sbn) ?: return false
            val remoteInput = replyAction.remoteInputs?.firstOrNull() ?: return false

            val bundle = Bundle()
            bundle.putCharSequence(remoteInput.resultKey, replyText)

            val intent = android.content.Intent()
            android.app.RemoteInput.addResultsToIntent(replyAction.remoteInputs, intent, bundle)

            replyAction.actionIntent.send(
                null, 0, intent, null, null
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Check karo yeh supported app hai ya nahi
    fun isSupportedApp(packageName: String): Boolean {
        return packageName in listOf(
            WHATSAPP_PACKAGE,
            WHATSAPP_BUSINESS_PACKAGE,
            TELEGRAM_PACKAGE,
            TELEGRAM_X_PACKAGE,
            INSTAGRAM_PACKAGE
        )
    }

    // Group notification hai ya nahi - group ko ignore karo
    fun isGroupNotification(sbn: StatusBarNotification): Boolean {
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        // WhatsApp group messages mein @ ya ":" hota hai title mein
        return sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
            || title.contains(":")
    }
}
