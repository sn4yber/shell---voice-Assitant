package com.shell.app.core.permissions

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.shell.app.domain.music.MusicPlatform

private val Context.shellPreferencesDataStore by preferencesDataStore(name = "shell_preferences")

data class ShellPreferencesState(
    val onboardingCompleted: Boolean = false,
    val assistantName: String = "Shell",
    val wakeWord: String = "Shell",
    val autoListen: Boolean = true,
    val musicPlatform: MusicPlatform = MusicPlatform.AskEveryTime,
    val importantNotificationPackages: Set<String> = emptySet(),
    val quietNotificationKeywords: Set<String> = setOf("promo", "promoción", "oferta", "sale", "discount", "ad", "publicidad")
)

class PermissionPreferences(context: Context) {
    private val dataStore = context.shellPreferencesDataStore

    val state: Flow<ShellPreferencesState> = dataStore.data.map { preferences ->
        ShellPreferencesState(
            onboardingCompleted = preferences[KEY_ONBOARDING_COMPLETED] ?: false,
            assistantName = preferences[KEY_ASSISTANT_NAME] ?: "Shell",
            wakeWord = preferences[KEY_WAKE_WORD] ?: "Shell",
            autoListen = preferences[KEY_AUTO_LISTEN] ?: true,
            musicPlatform = MusicPlatform.valueOf(preferences[KEY_MUSIC_PLATFORM] ?: MusicPlatform.AskEveryTime.name),
            importantNotificationPackages = preferences[KEY_IMPORTANT_NOTIFICATION_PACKAGES] ?: emptySet(),
            quietNotificationKeywords = preferences[KEY_QUIET_NOTIFICATION_KEYWORDS] ?: setOf("promo", "promoción", "oferta", "sale", "discount", "ad", "publicidad")
        )
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = value
        }
    }

    suspend fun setAssistantName(value: String) {
        dataStore.edit { preferences ->
            preferences[KEY_ASSISTANT_NAME] = value
        }
    }

    suspend fun setWakeWord(value: String) {
        dataStore.edit { preferences ->
            preferences[KEY_WAKE_WORD] = value
        }
    }

    suspend fun setAutoListen(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_LISTEN] = value
        }
    }

    suspend fun setMusicPlatform(value: MusicPlatform) {
        dataStore.edit { preferences ->
            preferences[KEY_MUSIC_PLATFORM] = value.name
        }
    }

    suspend fun setImportantNotificationPackages(value: Set<String>) {
        dataStore.edit { preferences ->
            preferences[KEY_IMPORTANT_NOTIFICATION_PACKAGES] = value
        }
    }

    suspend fun setQuietNotificationKeywords(value: Set<String>) {
        dataStore.edit { preferences ->
            preferences[KEY_QUIET_NOTIFICATION_KEYWORDS] = value
        }
    }

    fun updateQuietNotificationKeywordsFromCsv(csv: String) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[KEY_QUIET_NOTIFICATION_KEYWORDS] = csv
                    .split(',')
                    .map { it.trim().lowercase() }
                    .filter { it.isNotBlank() }
                    .toSet()
            }
        }
    }

    fun syncNotificationKeywordsToMemory() = runBlocking {
        val keywords = dataStore.data.first()[KEY_QUIET_NOTIFICATION_KEYWORDS]
            ?: setOf("promo", "promoción", "oferta", "sale", "discount", "ad", "publicidad")
        com.shell.app.data.notifications.NotificationCenter.updateQuietKeywords(keywords)
    }

    fun toggleImportantNotificationPackage(packageName: String, enabled: Boolean) {
        runBlocking {
            dataStore.edit { preferences ->
                val current = preferences[KEY_IMPORTANT_NOTIFICATION_PACKAGES] ?: emptySet()
                preferences[KEY_IMPORTANT_NOTIFICATION_PACKAGES] =
                    if (enabled) current + packageName else current - packageName
            }
        }
    }

    fun getImportantNotificationPackages(): Set<String> = runBlocking {
        dataStore.data.first()[KEY_IMPORTANT_NOTIFICATION_PACKAGES] ?: emptySet()
    }

    private companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("permission_onboarding_completed")
        val KEY_ASSISTANT_NAME = stringPreferencesKey("assistant_name")
        val KEY_WAKE_WORD = stringPreferencesKey("wake_word")
        val KEY_AUTO_LISTEN = booleanPreferencesKey("auto_listen")
        val KEY_MUSIC_PLATFORM = stringPreferencesKey("music_platform")
        val KEY_IMPORTANT_NOTIFICATION_PACKAGES = stringSetPreferencesKey("important_notification_packages")
        val KEY_QUIET_NOTIFICATION_KEYWORDS = stringSetPreferencesKey("quiet_notification_keywords")
    }
}
