package com.shell.app.presentation

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shell.app.core.permissions.PermissionPreferences
import com.shell.app.core.permissions.ShellPreferencesState
import com.shell.app.data.notifications.NotificationCenter
import com.shell.app.domain.music.MusicPlatform
import kotlinx.coroutines.launch

@Composable
fun ShellSettingsScreen(
    preferences: PermissionPreferences,
    preferencesState: ShellPreferencesState,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var assistantName by remember { mutableStateOf(preferencesState.assistantName) }
    var wakeWord by remember { mutableStateOf(preferencesState.wakeWord) }
    var autoListen by remember { mutableStateOf(preferencesState.autoListen) }
    var quietKeywordsCsv by remember { mutableStateOf(preferencesState.quietNotificationKeywords.joinToString(", ")) }
    var musicPlatform by remember { mutableStateOf(preferencesState.musicPlatform) }

    LaunchedEffect(preferencesState) {
        assistantName = preferencesState.assistantName
        wakeWord = preferencesState.wakeWord
        autoListen = preferencesState.autoListen
        quietKeywordsCsv = preferencesState.quietNotificationKeywords.joinToString(", ")
        musicPlatform = preferencesState.musicPlatform
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Ajustes de Shell")
                Text(text = "Configuración básica guardada en DataStore")

                TextField(
                    value = assistantName,
                    onValueChange = { assistantName = it },
                    label = { Text("Nombre del asistente") }
                )
                TextField(
                    value = wakeWord,
                    onValueChange = { wakeWord = it },
                    label = { Text("Wake word") }
                )

                Button(onClick = {
                    autoListen = !autoListen
                }) {
                    Text(text = if (autoListen) "Auto-listen: ON" else "Auto-listen: OFF")
                }

                Text(text = "Plataforma de música")
                Button(onClick = { musicPlatform = MusicPlatform.Spotify }) {
                    Text(text = if (musicPlatform == MusicPlatform.Spotify) "✓ Spotify" else "Spotify")
                }
                Button(onClick = { musicPlatform = MusicPlatform.YouTubeMusic }) {
                    Text(text = if (musicPlatform == MusicPlatform.YouTubeMusic) "✓ YouTube Music" else "YouTube Music")
                }
                Button(onClick = { musicPlatform = MusicPlatform.AskEveryTime }) {
                    Text(text = if (musicPlatform == MusicPlatform.AskEveryTime) "✓ Preguntarme" else "Preguntarme")
                }

                TextField(
                    value = quietKeywordsCsv,
                    onValueChange = { quietKeywordsCsv = it },
                    label = { Text("Palabras para ignorar") },
                    placeholder = { Text("promo, oferta, sale") }
                )

                Text(text = "Apps con notificaciones recientes")
                NotificationCenter.recent.forEach { notification ->
                    ImportantNotificationRow(
                        packageName = notification.packageName,
                        title = notification.title,
                        isImportant = preferencesState.importantNotificationPackages.contains(notification.packageName),
                        onToggle = { enabled ->
                            scope.launch {
                                preferences.toggleImportantNotificationPackage(notification.packageName, enabled)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        preferences.setAssistantName(assistantName.trim().ifBlank { "Shell" })
                        preferences.setWakeWord(wakeWord.trim().ifBlank { "Shell" })
                        preferences.setAutoListen(autoListen)
                        preferences.setMusicPlatform(musicPlatform)
                        preferences.updateQuietNotificationKeywordsFromCsv(quietKeywordsCsv)
                    }
                }) {
                    Text(text = "Guardar")
                }
                Button(onClick = onBack) {
                    Text(text = "Volver")
                }
            }
        }
    }
}

@Composable
private fun ImportantNotificationRow(
    packageName: String,
    title: String,
    isImportant: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val label = title.ifBlank { packageName }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label)
        Text(text = packageName)
        Switch(checked = isImportant, onCheckedChange = onToggle)
    }
}
