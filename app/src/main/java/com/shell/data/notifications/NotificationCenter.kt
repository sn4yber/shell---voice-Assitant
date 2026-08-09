package com.shell.app.data.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

data class NotificationSnapshot(
    val packageName: String = "",
    val title: String = "",
    val text: String = "",
    val isImportant: Boolean = false,
    val isEmpty: Boolean = true
)

object NotificationCenter {
    var latest by mutableStateOf(NotificationSnapshot())
        private set

    val recent = mutableStateListOf<NotificationSnapshot>()
    var quietKeywords by mutableStateOf(setOf("promo", "promoción", "oferta", "sale", "discount", "ad", "publicidad"))
        private set

    fun update(packageName: String, title: String, text: String, isImportant: Boolean) {
        val snapshot = NotificationSnapshot(
            packageName = packageName,
            title = title,
            text = text,
            isImportant = isImportant,
            isEmpty = false
        )
        latest = snapshot
        val existingIndex = recent.indexOfFirst { it.packageName == packageName }
        if (existingIndex >= 0) {
            recent[existingIndex] = snapshot
        } else {
            recent.add(0, snapshot)
        }

        while (recent.size > 6) {
            recent.removeAt(recent.lastIndex)
        }
    }

    fun updateQuietKeywords(keywords: Set<String>) {
        quietKeywords = keywords.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
    }

    fun shouldAnnounce(snapshot: NotificationSnapshot): Boolean {
        if (!snapshot.isImportant) return false
        val haystack = "${snapshot.title.lowercase()} ${snapshot.text.lowercase()}"
        return quietKeywords.none { keyword -> haystack.contains(keyword) }
    }
}
