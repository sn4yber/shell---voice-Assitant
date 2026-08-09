package com.shell.app

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shell.app.data.contacts.ContactLookupRepository
import com.shell.app.data.contacts.ContactWriteRepository
import com.shell.app.data.music.MusicController
import com.shell.app.data.phone.PhoneCallRepository
import com.shell.app.domain.commands.CommandInterpreter
import com.shell.app.domain.commands.ShellCommand
import com.shell.app.domain.commands.ShellCommand.CallContact
import com.shell.app.domain.music.MusicCommand
import com.shell.app.domain.music.MusicPlatform
import androidx.lifecycle.AndroidViewModel
import java.util.Calendar
import java.io.IOException
import android.os.RemoteException

class ShellViewModel(application: Application) : AndroidViewModel(application) {
    val appName: String = "SHELL"
    val subtitle: String = "Motorcycle Voice Assistant"
    private val contactLookupRepository = ContactLookupRepository(application.contentResolver)
    private val contactWriteRepository = ContactWriteRepository(application.contentResolver)
    private val phoneCallRepository = PhoneCallRepository(application.applicationContext)
    private val musicController = MusicController(application.applicationContext)

    var lastCommand by mutableStateOf("")
        private set

    var lastResponse by mutableStateOf("Esperando comando...")
        private set

    var lastContactMatch by mutableStateOf("")
        private set

    var awaitingContactField by mutableStateOf<ContactField?>(null)
        private set

    var pendingContactName by mutableStateOf("")
        private set

    var pendingContactNumber by mutableStateOf("")
        private set

    var lastMusicAction by mutableStateOf("")
        private set

    var musicPlatform by mutableStateOf(MusicPlatform.AskEveryTime)
        private set

    var pendingMusicSearchQuery by mutableStateOf<String?>(null)
        private set

    enum class ContactField {
        Name,
        Number
    }

    fun handleRecognizedSpeech(text: String): String {
        pendingMusicSearchQuery?.let { pendingQuery ->
            val platformChoice = resolvePlatformChoice(text)
            if (platformChoice != null) {
                pendingMusicSearchQuery = null
                return executeMusicSearch(pendingQuery, platformChoice)
            }
            if (text.trim().equals("cancelar", ignoreCase = true) || text.trim().equals("cancel", ignoreCase = true)) {
                pendingMusicSearchQuery = null
                lastResponse = "Búsqueda cancelada"
                return lastResponse
            }
        }

        if (awaitingContactField != null) {
            lastResponse = handlePendingContactField(text)
            return lastResponse
        }

        val command = CommandInterpreter.interpret(text)
        lastCommand = text
        lastResponse = when (command) {
            ShellCommand.TellTime -> currentTimeMessage()
            is CallContact -> handleCallCommand(command.query)
            is ShellCommand.Music -> handleMusicCommand(command.command)
            ShellCommand.Unknown -> "Todavía no entiendo ese comando."
        }
        return lastResponse
    }

    fun setMusicPlatform(platform: MusicPlatform) {
        musicPlatform = platform
    }

    private fun handleMusicCommand(command: com.shell.app.domain.music.MusicCommand): String {
        val response = when (command) {
            is MusicCommand.Search -> {
                if (musicPlatform == MusicPlatform.AskEveryTime) {
                    pendingMusicSearchQuery = command.query
                    "¿Spotify o YouTube Music?"
                } else {
                    executeMusicSearch(command.query, musicPlatform)
                }
            }
            else -> musicController.execute(command)
        }
        lastMusicAction = response
        return response
    }

    private fun executeMusicSearch(query: String, platform: MusicPlatform): String {
        val response = musicController.search(query, platform)
        lastMusicAction = response
        return response
    }

    private fun resolvePlatformChoice(text: String): MusicPlatform? {
        val normalized = text.trim().lowercase()
        return when {
            normalized.contains("spotify") -> MusicPlatform.Spotify
            normalized.contains("youtube") || normalized.contains("yt music") || normalized.contains("youtube music") -> MusicPlatform.YouTubeMusic
            else -> null
        }
    }

    private fun handleCallCommand(query: String): String {
        val permissionGranted = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            lastContactMatch = ""
            return "Necesito permiso de contactos para buscar ese número."
        }

        val match = contactLookupRepository.findFirstMatch(query)
        return if (match != null) {
            lastContactMatch = "${match.name} · ${match.phoneNumber}"
            phoneCallRepository.startCall(match.phoneNumber)
            "Llamando a ${match.name}"
        } else {
            lastContactMatch = ""
            if (containsDigits(query)) {
                pendingContactNumber = query.filter(Char::isDigit)
                pendingContactName = ""
                awaitingContactField = ContactField.Name
                "Número desconocido. ¿Con qué nombre lo agrego?"
            } else {
                pendingContactName = query
                pendingContactNumber = ""
                awaitingContactField = ContactField.Number
                "No lo encontré. Dime el número para agregarlo."
            }
        }
    }

    private fun handlePendingContactField(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            return "No te escuché bien. Repite el dato, por favor."
        }

        return when (awaitingContactField) {
            ContactField.Name -> {
                pendingContactName = trimmed
                addPendingContact()
            }
            ContactField.Number -> {
                pendingContactNumber = trimmed.filter(Char::isDigit)
                if (pendingContactNumber.isBlank()) {
                    "Dime solo el número para guardarlo."
                } else {
                    addPendingContact()
                }
            }
            null -> "No hay ningún contacto pendiente."
        }
    }

    private fun addPendingContact(): String {
        val name = pendingContactName.trim()
        val number = pendingContactNumber.trim()

        if (name.isBlank()) {
            awaitingContactField = ContactField.Name
            return "Dime el nombre para guardarlo."
        }

        if (number.isBlank()) {
            awaitingContactField = ContactField.Number
            return "Dime el número para guardarlo."
        }

        val permissionGranted = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            awaitingContactField = ContactField.Name
            return "Necesito permiso de contactos para guardarlo."
        }

        try {
            contactWriteRepository.insertContact(name, number)
        } catch (exception: RemoteException) {
            awaitingContactField = ContactField.Name
            lastResponse = "No pude agregar el contacto."
            return lastResponse
        } catch (exception: IOException) {
            awaitingContactField = ContactField.Name
            lastResponse = "No pude agregar el contacto."
            return lastResponse
        } catch (exception: android.content.OperationApplicationException) {
            awaitingContactField = ContactField.Name
            lastResponse = "No pude agregar el contacto."
            return lastResponse
        }

        awaitingContactField = null
        lastContactMatch = "$name · $number"
        phoneCallRepository.startCall(number)
        lastResponse = "Llamando a $name"
        return "Llamando a $name"
    }

    private fun containsDigits(value: String): Boolean {
        return value.any(Char::isDigit)
    }

    private fun currentTimeMessage(): String {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        return String.format("Son las %02d:%02d", hour, minute)
    }
}
