package com.shell.app.data.music

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.view.KeyEvent
import com.shell.app.domain.music.MusicCommand
import com.shell.app.domain.music.MusicPlatform
import java.net.URLEncoder

class MusicController(context: Context) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun execute(command: MusicCommand): String {
        return when (command) {
            MusicCommand.Play -> {
                dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
                "Reproduciendo música"
            }
            MusicCommand.Pause -> {
                dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
                "Música en pausa"
            }
            MusicCommand.Next -> {
                dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                "Siguiente canción"
            }
            MusicCommand.Previous -> {
                dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                "Canción anterior"
            }
            MusicCommand.VolumeUp -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                "Subiendo volumen"
            }
            MusicCommand.VolumeDown -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                "Bajando volumen"
            }
            MusicCommand.Unknown -> "No entendí el comando de música"
            is MusicCommand.Search -> "Buscando ${command.query}"
        }
    }

    fun search(query: String, platform: MusicPlatform): String {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return "Dime qué quieres buscar"

        val intent = when (platform) {
            MusicPlatform.Spotify -> buildViewIntent("https://open.spotify.com/search/${encoded(trimmed)}")
            MusicPlatform.YouTubeMusic -> buildViewIntent("https://music.youtube.com/search?q=${encoded(trimmed)}")
            MusicPlatform.AskEveryTime -> return "Necesito que elijas Spotify o YouTube Music"
        }

        return if (intent.resolveActivity(appContext.packageManager) != null) {
            appContext.startActivity(intent)
            "Buscando $trimmed en ${platform.label}"
        } else {
            "No pude abrir ${platform.label}"
        }
    }

    private fun dispatchMediaKey(keyCode: Int) {
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager.dispatchMediaKeyEvent(downEvent)
        audioManager.dispatchMediaKeyEvent(upEvent)
    }

    private fun buildViewIntent(url: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun encoded(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name())
    }
}
