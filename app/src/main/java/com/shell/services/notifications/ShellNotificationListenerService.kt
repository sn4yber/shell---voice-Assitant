package com.shell.app.services.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import com.shell.app.data.notifications.NotificationCenter
import com.shell.app.core.permissions.PermissionPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ShellNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val isImportant = isImportantNotification(sbn.packageName, title, text)

        NotificationCenter.update(
            packageName = sbn.packageName,
            title = title,
            text = text,
            isImportant = isImportant
        )
    }

    private fun isImportantNotification(packageName: String, title: String, text: String): Boolean {
        val preferences = PermissionPreferences(applicationContext)
        val importantPackages = runBlocking {
            preferences.state.first().importantNotificationPackages
        }
        if (!importantPackages.contains(packageName)) return false

        val quietKeywords = runBlocking {
            preferences.state.first().quietNotificationKeywords
        }
        val haystack = "${title.lowercase()} ${text.lowercase()}"
        return quietKeywords.none { keyword -> keyword.isNotBlank() && haystack.contains(keyword.lowercase()) }
    }
}
