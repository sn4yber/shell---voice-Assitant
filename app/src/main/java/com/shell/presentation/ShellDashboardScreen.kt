package com.shell.app.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.shell.app.ShellViewModel
import com.shell.app.data.notifications.NotificationCenter
import com.shell.app.core.bluetooth.BluetoothMonitor
import com.shell.app.domain.music.MusicPlatform
import com.shell.app.voice.VoiceController

@Composable
fun ShellDashboardScreen(
    viewModel: ShellViewModel,
    permissionsReady: Boolean,
    autoListenEnabled: Boolean,
    musicPlatform: MusicPlatform,
    onManagePermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onMusicPlatformChange: (MusicPlatform) -> Unit
) {
    val context = LocalContext.current
    val voiceController = remember(context) { VoiceController(context) }
    val bluetoothMonitor = remember(context) { BluetoothMonitor(context) }
    val voiceState = voiceController.state
    val bluetoothState = bluetoothMonitor.state
    val notificationState = NotificationCenter.latest
    var drivingModeEnabled by remember { mutableStateOf(bluetoothState.isConnected) }
    var lastAnnouncedNotificationKey by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceController.startListening()
        }
    }

    DisposableEffect(Unit) {
        bluetoothMonitor.start()
        onDispose {
            bluetoothMonitor.stop()
            voiceController.release()
        }
    }

    LaunchedEffect(permissionsReady, autoListenEnabled) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionsReady && autoListenEnabled && granted) {
            voiceController.startListening()
        } else if (!autoListenEnabled || !permissionsReady || !granted) {
            voiceController.stopListening()
        }
    }

    LaunchedEffect(bluetoothState.isConnected) {
        drivingModeEnabled = bluetoothState.isConnected
    }

    LaunchedEffect(voiceState.status, voiceState.recognizedText) {
        if (voiceState.status == "Recognized" && voiceState.recognizedText.isNotBlank()) {
            val response = viewModel.handleRecognizedSpeech(voiceState.recognizedText)
            voiceController.speak(response)
        }
    }

    LaunchedEffect(notificationState.packageName, notificationState.title, notificationState.text, notificationState.isImportant, drivingModeEnabled) {
        if (drivingModeEnabled && notificationState.isImportant && !notificationState.isEmpty) {
            val notificationKey = "${notificationState.packageName}|${notificationState.title}|${notificationState.text}"
            if (notificationKey != lastAnnouncedNotificationKey) {
                val announcement = buildString {
                    append("Notificación importante")
                    if (notificationState.title.isNotBlank()) {
                        append(": ")
                        append(notificationState.title)
                    }
                    if (notificationState.text.isNotBlank()) {
                        append(". ")
                        append(notificationState.text)
                    }
                }
                lastAnnouncedNotificationKey = notificationKey
                voiceController.speak(announcement)
            }
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = viewModel.appName)
                Text(text = viewModel.subtitle)
                Text(text = "Estado: ${voiceState.status}")
                Text(text = "Reconocido: ${voiceState.recognizedText.ifBlank { "—" }}")
                Text(text = "Respuesta: ${viewModel.lastResponse}")
                Text(text = "Contacto: ${viewModel.lastContactMatch.ifBlank { "—" }}")
                Text(text = "Pendiente: ${viewModel.awaitingContactField?.name ?: "—"}")
                Text(text = "TTS: ${if (voiceState.isTtsReady) "Listo" else "No listo"}")
                Text(text = if (permissionsReady) "Permisos listos" else "Permisos incompletos")
                Text(text = if (bluetoothState.isBluetoothOn) "Bluetooth encendido" else "Bluetooth apagado")
                Text(text = bluetoothState.connectedLabel)
                Text(text = if (drivingModeEnabled) "🏍️ Modo conducción ON" else "🏍️ Modo conducción OFF")
                Text(text = if (notificationState.isEmpty) "Sin notificaciones" else "🔔 ${notificationState.title.ifBlank { notificationState.packageName }}")
                Text(text = if (notificationState.isEmpty) "" else notificationState.text.ifBlank { "Sin texto" })
                Text(text = if (notificationState.isImportant) "✅ Marcada como importante" else "⚪ No marcada como importante")
                Text(text = "Música: ${viewModel.lastMusicAction.ifBlank { "—" }}")
                Text(text = "Plataforma música: ${musicPlatform.label}")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onManagePermissions) {
                    Text(text = "Administrar permisos")
                }
                Button(onClick = onOpenSettings) {
                    Text(text = "Ajustes")
                }
                Button(onClick = { onMusicPlatformChange(MusicPlatform.Spotify) }) {
                    Text(text = "Spotify")
                }
                Button(onClick = { onMusicPlatformChange(MusicPlatform.YouTubeMusic) }) {
                    Text(text = "YouTube Music")
                }
                Button(onClick = { onMusicPlatformChange(MusicPlatform.AskEveryTime) }) {
                    Text(text = "Preguntarme")
                }
                Button(onClick = {
                    drivingModeEnabled = !drivingModeEnabled
                }) {
                    Text(text = if (drivingModeEnabled) "Desactivar modo conducción" else "Activar modo conducción")
                }
                Button(onClick = { viewModel.handleRecognizedSpeech("reproduce música") }) {
                    Text(text = "Play")
                }
                Button(onClick = { viewModel.handleRecognizedSpeech("pausa música") }) {
                    Text(text = "Pause")
                }
                Button(onClick = { viewModel.handleRecognizedSpeech("siguiente canción") }) {
                    Text(text = "Next")
                }
                Button(onClick = { viewModel.handleRecognizedSpeech("canción anterior") }) {
                    Text(text = "Previous")
                }
                Button(onClick = { viewModel.handleRecognizedSpeech("volumen sube") }) {
                    Text(text = "Volumen +")
                }
                Button(onClick = { viewModel.handleRecognizedSpeech("volumen baja") }) {
                    Text(text = "Volumen -")
                }
                Button(onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        voiceController.startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }) {
                    Text(text = if (voiceState.isListening) "Escuchando..." else "Escuchar")
                }
                Button(onClick = {
                    voiceController.speak(
                        voiceState.recognizedText.ifBlank { "Shell listo para empezar." }
                    )
                }) {
                    Text(text = "Probar TTS")
                }
                Text(text = if (autoListenEnabled) "Escucha automática: ON" else "Escucha automática: OFF")
                Text(text = if (permissionsReady) "🟢 Sistema listo para voz" else "⚠️ Falta completar permisos")
            }
        }
    }
}
