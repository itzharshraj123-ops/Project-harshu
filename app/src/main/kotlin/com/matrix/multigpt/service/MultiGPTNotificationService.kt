package com.matrix.multigpt.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MultiGPTNotificationService : NotificationListenerService() {

    @Inject
    lateinit var autoReplyManager: AutoReplyManager

    @Inject
    lateinit var notificationReplyHelper: NotificationReplyHelper

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        // Sirf supported apps ke notifications handle karo
        if (!notificationReplyHelper.isSupportedApp(sbn.packageName)) return

        // Apni hi app ka notification ignore karo
        if (sbn.packageName == packageName) return

        autoReplyManager.handleNotification(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Kuch nahi karna
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
    }
}
