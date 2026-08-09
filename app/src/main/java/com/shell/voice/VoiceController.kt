package com.shell.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

data class VoiceState(
    val isListening: Boolean = false,
    val isSpeechReady: Boolean = false,
    val isTtsReady: Boolean = false,
    val status: String = "Idle",
    val recognizedText: String = "",
    val spokenText: String = ""
)

class VoiceController(context: Context) : RecognitionListener, TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
    private val textToSpeech = TextToSpeech(appContext, this)

    var state by mutableStateOf(VoiceState())
        private set

    init {
        speechRecognizer.setRecognitionListener(this)
        state = state.copy(
            isSpeechReady = SpeechRecognizer.isRecognitionAvailable(appContext),
            status = if (SpeechRecognizer.isRecognitionAvailable(appContext)) "Ready" else "Speech recognizer unavailable"
        )
    }

    fun startListening() {
        if (!state.isSpeechReady) {
            state = state.copy(status = "Speech recognizer unavailable")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        state = state.copy(isListening = true, status = "Listening...")
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        state = state.copy(isListening = false, status = "Idle")
    }

    fun speak(text: String) {
        if (!state.isTtsReady) {
            state = state.copy(status = "TTS not ready")
            return
        }

        state = state.copy(spokenText = text, status = "Speaking")
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "shell-tts")
    }

    override fun onInit(status: Int) {
        val ready = status == TextToSpeech.SUCCESS
        if (ready) {
            val languageResult = textToSpeech.setLanguage(Locale.getDefault())
            state = state.copy(
                isTtsReady = languageResult != TextToSpeech.LANG_MISSING_DATA &&
                    languageResult != TextToSpeech.LANG_NOT_SUPPORTED,
                status = if (languageResult == TextToSpeech.LANG_MISSING_DATA || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    "TTS language unsupported"
                } else {
                    "Ready"
                }
            )
        } else {
            state = state.copy(status = "TTS init failed")
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        state = state.copy(isListening = true, status = "Listening...")
    }

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(rmsdB: Float) = Unit

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        state = state.copy(isListening = false, status = "Processing...")
    }

    override fun onError(error: Int) {
        state = state.copy(isListening = false, status = "Speech error: $error")
    }

    override fun onResults(results: Bundle?) {
        val text = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            .orEmpty()

        state = state.copy(
            isListening = false,
            recognizedText = text,
            status = if (text.isBlank()) "No speech recognized" else "Recognized"
        )
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val text = partialResults
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            .orEmpty()

        if (text.isNotBlank()) {
            state = state.copy(recognizedText = text)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    fun release() {
        speechRecognizer.destroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}
